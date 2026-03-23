import pytest
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import os

@pytest.fixture(scope="session")
def base_url():
    """Базовый URL для тестов"""
    return f"file://{os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))}"

@pytest.fixture
def chrome_options():
    """Настройки Chrome для тестов"""
    from selenium.webdriver.chrome.options import Options
    options = Options()
    # Раскомментировать для headless режима
    # options.add_argument('--headless')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    options.add_argument('--disable-gpu')
    return options

@pytest.fixture
def driver(chrome_options):
    """Основной драйвер для тестов"""
    service = Service(ChromeDriverManager().install())
    driver = webdriver.Chrome(service=service, options=chrome_options)
    driver.implicitly_wait(10)
    driver.maximize_window()
    yield driver
    driver.quit()