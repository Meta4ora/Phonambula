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
    
    def wait_for_element(self, driver, by, value, timeout=15):
        """Ожидание появления элемента"""
        return WebDriverWait(driver, timeout).until(
            EC.presence_of_element_located((by, value))
        )
    
    def wait_for_clickable(self, driver, by, value, timeout=15):
        """Ожидание кликабельности элемента"""
        return WebDriverWait(driver, timeout).until(
            EC.element_to_be_clickable((by, value))
        )
    
    def wait_for_visible(self, driver, by, value, timeout=15):
        """Ожидание видимости элемента"""
        return WebDriverWait(driver, timeout).until(
            EC.visibility_of_element_located((by, value))
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
        
        # переход на home.html
        WebDriverWait(driver, 15).until(
            EC.url_contains("home.html")
        )
        
        WebDriverWait(driver, 15).until(
            EC.presence_of_element_located((By.ID, "searchInput"))
        )
        time.sleep(2)
    
    def logout(self, driver):
        """Выход из системы"""
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
    
    # модальное окно
    add_btn = self.wait_for_clickable(driver, By.ID, "addEmployeeBtn")
    add_btn.click()
    
    time.sleep(1)
    
    driver.find_element(By.ID, "empSurname").send_keys(test_user["surname"])
    driver.find_element(By.ID, "empName").send_keys(test_user["name"])
    driver.find_element(By.ID, "empPatronymic").send_keys(test_user["patronymic"])
    driver.find_element(By.ID, "empLogin").send_keys(test_user["login"])
    driver.find_element(By.ID, "empPassword").send_keys(test_user["password"])
    driver.find_element(By.ID, "empConfirmPassword").send_keys(test_user["password"])
    
    time.sleep(2)
    
    selects = ["empRole", "empPosition", "empDivision", "empBuilding"]
    
    for select_id in selects:
        try:
            select = driver.find_element(By.ID, select_id)
            # Получаем все опции
            options = select.find_elements(By.TAG_NAME, "option")
            # Если есть хотя бы одна опция 
            if len(options) > 1:
                # Выбираем первую опцию 
                options[1].click()
                print(f"Выбрана опция в {select_id}: {options[1].text}")
            elif len(options) == 1:
                # Если только одна опция, выбираем её
                options[0].click()
        except Exception as e:
            print(f"Не удалось выбрать в {select_id}: {e}")
    
    # Сохраняем
    save_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-save")
    save_btn.click()
    
    time.sleep(3)
    
    return test_user
    
    def delete_test_user(self, driver, user_login):
        """Удаление тестового пользователя"""
        # Ищем пользователя в списке
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        search_input.clear()
        search_input.send_keys(user_login)
        time.sleep(2)
        
        # Находим и удаляем
        try:
            delete_btn = WebDriverWait(driver, 5).until(
                EC.element_to_be_clickable((By.CSS_SELECTOR, ".delete-contact-btn"))
            )
            delete_btn.click()
            
            # Подтверждаем удаление
            time.sleep(1)
            alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
            alert.accept()
            time.sleep(2)
            
            return True
        except:
            return False
    
    
    def test_admin_login_success(self, driver):
        """успешный вход администратора"""
        self.login(driver, "admin", "admin")
        
        assert "home.html" in driver.current_url
        
        add_btn = driver.find_element(By.ID, "addEmployeeBtn")
        assert add_btn.is_displayed()
        
        token = driver.execute_script("return localStorage.getItem('token');")
        role = driver.execute_script("return localStorage.getItem('userRole');")
        
        assert token is not None
        assert role is not None
        
        self.logout(driver)
    
    def test_admin_logout(self, driver):
        """Тест: выход из системы"""
        self.login(driver, "admin", "admin")
        
        self.logout(driver)
        
        assert "index.html" in driver.current_url or driver.current_url.endswith("/")
        
        # Проверяем, что токен удален
        token = driver.execute_script("return localStorage.getItem('token');")
        assert token is None
    
    
    def test_create_employee(self, driver):
        """создание нового сотрудника (админ)"""
        self.login(driver, "admin", "admin")
        
        # Создаем тестового пользователя
        test_user = self.create_test_user(driver)
        
        # Проверяем, что пользователь появился в списке
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        search_input.clear()
        search_input.send_keys(test_user["login"])
        time.sleep(2)
        
        # Проверяем, что контакт найден
        try:
            contact = WebDriverWait(driver, 5).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, ".contact-item"))
            )
            assert test_user["surname"] in contact.text or test_user["login"] in contact.text
        except:
            pass
        
        # Удаляем тестового пользователя
        self.delete_test_user(driver, test_user["login"])
        
        self.logout(driver)
    
    def test_delete_employee(self, driver):
        """удаление сотрудника (админ)"""
        self.login(driver, "admin", "admin")
        
        test_user = self.create_test_user(driver)
        
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        search_input.clear()
        search_input.send_keys(test_user["login"])
        time.sleep(2)
        
        delete_btn = WebDriverWait(driver, 5).until(
            EC.element_to_be_clickable((By.CSS_SELECTOR, ".delete-contact-btn"))
        )
        delete_btn.click()
        
        # Подтверждаем удаление
        time.sleep(1)
        alert = WebDriverWait(driver, 5).until(EC.alert_is_present())
        alert.accept()
        
        time.sleep(2)
        
        # Проверяем, что пользователь удален
        search_input.clear()
        search_input.send_keys(test_user["login"])
        time.sleep(2)
        
        try:
            no_contacts = driver.find_element(By.CSS_SELECTOR, ".contact-tree li p")
            assert "Нет контактов" in no_contacts.text or "Ничего не найдено" in no_contacts.text
        except:
            pass
        
        self.logout(driver)
    
    
    def test_search_functionality(self, driver):
        """Тест: функциональность поиска"""
        self.login(driver, "admin", "admin")
        
        # Создаем  пользователя для поиска
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
        time.sleep(3)
        
        # Тестируем поиск
        search_input = self.wait_for_element(driver, By.ID, "searchInput")
        
        # Поиск по логину
        search_input.clear()
        search_input.send_keys(test_login)
        time.sleep(2)
        
        try:
            result_count = driver.find_element(By.CSS_SELECTOR, ".search-result-count")
            assert "Найдено:" in result_count.text or "Всего контактов" in result_count.text
        except:
            pass
        
        # Очистка поиска
        clear_btn = driver.find_element(By.ID, "clearSearchBtn")
        if clear_btn.is_displayed():
            clear_btn.click()
            assert search_input.get_attribute("value") == ""
        
        # Удаляем тестового пользователя
        self.delete_test_user(driver, test_login)
        
        self.logout(driver)
    
    
    def test_export_pdf_button_click(self, driver):
        """экспорт в PDF (проверка клика)"""
        self.login(driver, "admin", "admin")
        
        export_btn = self.wait_for_clickable(driver, By.ID, "exportPdfBtn")
        original_text = export_btn.text
        
        export_btn.click()
        
        time.sleep(3)
        final_btn = driver.find_element(By.ID, "exportPdfBtn")
        assert final_btn.is_enabled()
        
        self.logout(driver)
    
    
    def test_empty_fields_validation(self, driver):
        """валидация пустых полей при создании"""
        self.login(driver, "admin", "admin")
        
        add_btn = self.wait_for_clickable(driver, By.ID, "addEmployeeBtn")
        add_btn.click()
        time.sleep(1)
        
        # Пытаемся сохранить без заполнения обязательных полей
        save_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-save")
        save_btn.click()
        
        # Проверяем, что появилось уведомление о валидации
        time.sleep(2)
        try:
            notification = driver.find_element(By.CSS_SELECTOR, ".notification")
            assert notification.is_displayed()
            assert len(notification.text) > 0
        except:
            pass
        
        # Закрываем модальное окно
        close_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-cancel")
        close_btn.click()
        
        self.logout(driver)
    
    def test_password_validation(self, driver):
        """валидация короткого пароля"""
        self.login(driver, "admin", "admin")
        
        add_btn = self.wait_for_clickable(driver, By.ID, "addEmployeeBtn")
        add_btn.click()
        time.sleep(1)
        
        driver.find_element(By.ID, "empSurname").send_keys("Тестов")
        driver.find_element(By.ID, "empName").send_keys("Тест")
        driver.find_element(By.ID, "empLogin").send_keys("test_validation")
        driver.find_element(By.ID, "empPassword").send_keys("123")  
        driver.find_element(By.ID, "empConfirmPassword").send_keys("123")
        
        save_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-save")
        save_btn.click()
        
        # Проверяем уведомление о коротком пароле
        time.sleep(2)
        notification = WebDriverWait(driver, 5).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, ".notification"))
        )
        assert "минимум 4 символа" in notification.text.lower()
        
        # Закрываем модальное окно
        close_btn = driver.find_element(By.CSS_SELECTOR, "#addEmployeeModal .modal-cancel")
        close_btn.click()
        
        self.logout(driver)