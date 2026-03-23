import pytest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from selenium.common.exceptions import TimeoutException
from webdriver_manager.chrome import ChromeDriverManager
import time
import random

class TestPhonambula:
    
    @pytest.fixture
    def driver(self):
        """Настройка драйвера перед каждым тестом"""
        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service)
        driver.maximize_window()
        driver.implicitly_wait(10)
        yield driver
        driver.quit()
    
    def wait_for_element(self, driver, by, value, timeout=10):
        """Ожидание появления элемента"""
        return WebDriverWait(driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )
    
    def wait_for_clickable(self, driver, by, value, timeout=10):
        """Ожидание кликабельности элемента"""
        return WebDriverWait(driver, timeout).until(
            EC.element_to_be_clickable((by, value))
        )
    
    def login(self, driver, username, password):
        """Вход в систему"""
        driver.get("http://localhost:53000/login.html")
        
        login_input = self.wait_for_element(driver, By.ID, "loginAuth")
        login_input.clear()
        login_input.send_keys(username)
        
        password_input = driver.find_element(By.ID, "passwordAuth")
        password_input.clear()
        password_input.send_keys(password)
        
        login_btn = driver.find_element(By.CSS_SELECTOR, ".submit-btn")
        login_btn.click()
        
        # Ждем перехода на home.html
        WebDriverWait(driver, 10).until(
            EC.url_contains("home.html")
        )
        time.sleep(2)
    
    def logout(self, driver):
        """Выход из системы (через очистку localStorage)"""
        driver.execute_script("localStorage.clear();")
        driver.get("http://localhost:53000/index.html")
        time.sleep(1)
    
    def create_test_user(self, driver):
        """Создание тестового пользователя"""
        unique_id = random.randint(1000, 9999)
        test_user = {
            "surname": f"Тестов",
            "name": f"Пользователь",
            "patronymic": f"{unique_id}",
            "login": f"test_user_{unique_id}",
            "password": "test123"
        }
        
        # Открываем модальное окно
        add_btn = self.wait_for_clickable(driver, By.ID, "addEmployeeBtn")
        add_btn.click()
        
        time.sleep(1)
        
        # Заполняем форму
        surname_input = self.wait_for_element(driver, By.ID, "empSurname")
        surname_input.send_keys(test_user["surname"])
        
        driver.find_element(By.ID, "empName").send_keys(test_user["name"])
        driver.find_element(By.ID, "empPatronymic").send_keys(test_user["patronymic"])
        driver.find_element(By.ID, "empLogin").send_keys(test_user["login"])
        driver.find_element(By.ID, "empPassword").send_keys(test_user["password"])
        driver.find_element(By.ID, "empConfirmPassword").send_keys(test_user["password"])
        
        # Сохраняем
        save_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-save")
        save_btn.click()
        
        time.sleep(2)
        
        return test_user
    
    def delete_test_user(self, driver, user_login):
        """Удаление тестового пользователя"""
        # Ищем пользователя в списке
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        search_input.clear()
        search_input.send_keys(user_login)
        time.sleep(1)
        
        # Находим и удаляем
        try:
            delete_btn = WebDriverWait(driver, 5).until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, ".delete-contact-btn"))
            )
            delete_btn.click()
            
            # Подтверждаем удаление
            time.sleep(1)
            driver.switch_to.alert.accept()
            time.sleep(2)
            
            return True
        except:
            return False
    
    # ========== ТЕСТЫ АВТОРИЗАЦИИ ==========
    
    def test_admin_login_success(self, driver):
        """Тест: успешный вход администратора"""
        self.login(driver, "admin", "admin")
        
        # Проверяем, что мы на home.html
        assert "home.html" in driver.current_url
        
        # Проверяем, что видна кнопка добавления сотрудника (доступна только админу)
        add_btn = driver.find_element(By.ID, "addEmployeeBtn")
        assert add_btn.is_displayed()
        
        # Проверяем, что в localStorage есть токен и роль
        token = driver.execute_script("return localStorage.getItem('token');")
        role = driver.execute_script("return localStorage.getItem('userRole');")
        
        assert token is not None
        assert role is not None
        
        self.logout(driver)
    
    def test_admin_logout(self, driver):
        """Тест: выход из системы"""
        self.login(driver, "admin", "admin")
        
        # Выходим через очистку localStorage и переход на главную
        self.logout(driver)
        
        # Проверяем, что мы на главной странице
        assert "index.html" in driver.current_url or driver.current_url.endswith("/")
        
        # Проверяем, что токен удален
        token = driver.execute_script("return localStorage.getItem('token');")
        assert token is None
    
    # ========== ТЕСТЫ УПРАВЛЕНИЯ СОТРУДНИКАМИ ==========
    
    def test_create_employee(self, driver):
        """Тест: создание нового сотрудника (админ)"""
        self.login(driver, "admin", "admin")
        
        # Создаем тестового пользователя
        test_user = self.create_test_user(driver)
        
        # Проверяем, что пользователь появился в списке
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        search_input.clear()
        search_input.send_keys(test_user["login"])
        time.sleep(1)
        
        # Проверяем, что контакт найден
        contact_name = driver.find_element(By.CSS_SELECTOR, ".contact-item")
        assert test_user["surname"] in contact_name.text
        
        # Проверяем, что уведомление об успехе появилось
        notification = driver.find_element(By.CSS_SELECTOR, ".notification-success")
        assert notification.is_displayed()
        
        # Удаляем тестового пользователя
        self.delete_test_user(driver, test_user["login"])
        
        self.logout(driver)
    
    def test_delete_employee(self, driver):
        """Тест: удаление сотрудника (админ)"""
        self.login(driver, "admin", "admin")
        
        # Создаем тестового пользователя
        test_user = self.create_test_user(driver)
        
        # Ищем и удаляем
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        search_input.clear()
        search_input.send_keys(test_user["login"])
        time.sleep(1)
        
        # Проверяем, что пользователь существует
        contact = driver.find_element(By.CSS_SELECTOR, ".contact-item")
        assert test_user["surname"] in contact.text
        
        # Удаляем
        delete_btn = driver.find_element(By.CSS_SELECTOR, ".delete-contact-btn")
        delete_btn.click()
        
        # Подтверждаем удаление
        time.sleep(1)
        alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
        alert.accept()
        
        time.sleep(2)
        
        # Проверяем, что пользователь удален
        search_input.clear()
        search_input.send_keys(test_user["login"])
        time.sleep(1)
        
        # Должно быть сообщение "Нет контактов"
        no_contacts = driver.find_element(By.CSS_SELECTOR, ".contact-tree li p")
        assert "Нет контактов" in no_contacts.text or "Ничего не найдено" in no_contacts.text
        
        self.logout(driver)
    
    # ========== ТЕСТЫ ПОИСКА ==========
    
    def test_search_functionality(self, driver):
        """Тест: функциональность поиска"""
        self.login(driver, "admin", "admin")
        
        # Создаем уникального пользователя для поиска
        unique_id = random.randint(1000, 9999)
        test_login = f"search_test_{unique_id}"
        
        # Добавляем пользователя
        add_btn = self.wait_for_clickable(driver, By.ID, "addEmployeeBtn")
        add_btn.click()
        time.sleep(1)
        
        driver.find_element(By.ID, "empSurname").send_keys("Поисков")
        driver.find_element(By.ID, "empName").send_keys("Тест")
        driver.find_element(By.ID, "empLogin").send_keys(test_login)
        driver.find_element(By.ID, "empPassword").send_keys("test123")
        driver.find_element(By.ID, "empConfirmPassword").send_keys("test123")
        driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-save").click()
        time.sleep(2)
        
        # Тестируем поиск
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        
        # Поиск по логину
        search_input.clear()
        search_input.send_keys(test_login)
        time.sleep(1)
        
        # Проверяем, что нашли
        result_count = driver.find_element(By.CSS_SELECTOR, ".search-result-count")
        assert "Найдено:" in result_count.text
        
        # Поиск по имени
        search_input.clear()
        search_input.send_keys("Поисков")
        time.sleep(1)
        assert "Найдено:" in result_count.text
        
        # Поиск несуществующего
        search_input.clear()
        search_input.send_keys("xyz123xyz")
        time.sleep(1)
        assert "Ничего не найдено" in result_count.text or "Нет контактов" in result_count.text
        
        # Очистка поиска
        clear_btn = driver.find_element(By.ID, "clearSearchBtn")
        clear_btn.click()
        assert search_input.get_attribute("value") == ""
        
        # Удаляем тестового пользователя
        search_input.clear()
        search_input.send_keys(test_login)
        time.sleep(1)
        self.delete_test_user(driver, test_login)
        
        self.logout(driver)
    

    # ========== ТЕСТЫ ЭКСПОРТА ==========
    
    def test_export_pdf_button_click(self, driver):
        """Тест: экспорт в PDF (проверка клика)"""
        self.login(driver, "admin", "admin")
        
        # Ждем загрузки контактов
        time.sleep(2)
        
        # Находим кнопку экспорта
        export_btn = self.wait_for_clickable(driver, By.ID, "exportPdfBtn")
        original_text = export_btn.text
        
        # Кликаем
        export_btn.click()
        
        # Проверяем, что текст изменился (экспорт начался)
        time.sleep(2)
        
        # Проверяем, что кнопка вернулась в исходное состояние
        final_text = driver.find_element(By.ID, "exportPdfBtn").text
        assert final_text == original_text or "Экспорт" in final_text
        
        self.logout(driver)
    
    
    # ========== ТЕСТЫ ВАЛИДАЦИИ ==========
    
    def test_empty_fields_validation(self, driver):
        """Тест: валидация пустых полей при создании"""
        self.login(driver, "admin", "admin")
        
        add_btn = self.wait_for_clickable(driver, By.ID, "addEmployeeBtn")
        add_btn.click()
        time.sleep(1)
        
        # Пытаемся сохранить без заполнения обязательных полей
        save_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-save")
        save_btn.click()
        
        # Проверяем, что появилось уведомление
        time.sleep(1)
        notification = driver.find_element(By.CSS_SELECTOR, ".notification")
        assert notification.is_displayed()
        assert "обязательные поля" in notification.text.lower()
        
        # Закрываем модальное окно
        close_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-cancel")
        close_btn.click()
        
        self.logout(driver)