// Функция для показа красивых уведомлений
function showNotification(message, type = 'error') {
    // Удаляем существующее уведомление
    const existingNotification = document.querySelector('.custom-notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    // Создаем элемент уведомления
    const notification = document.createElement('div');
    notification.className = `custom-notification custom-notification-${type}`;
    
    // Иконка в зависимости от типа
    const icons = {
        success: '✓',
        error: '✗',
        info: 'ℹ',
        warning: '⚠'
    };
    
    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-icon">${icons[type] || icons.error}</span>
            <span class="notification-message">${message}</span>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Автоматическое закрытие через 3 секунды
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease forwards';
        setTimeout(() => {
            if (notification.parentNode) notification.remove();
        }, 300);
    }, 3000);
    
    // Закрытие по клику
    notification.addEventListener('click', () => {
        notification.style.animation = 'slideOutRight 0.3s ease forwards';
        setTimeout(() => {
            if (notification.parentNode) notification.remove();
        }, 300);
    });
}

function showRegister(pushState = true) {
    const loginCard = document.getElementById('loginCard');
    const registerCard = document.getElementById('registerCard');

    loginCard.classList.remove('active');
    loginCard.classList.add('exit-left');

    registerCard.classList.remove('exit-left');
    registerCard.classList.add('active');

    if (pushState) {
        history.pushState({ form: 'register' }, '', '?form=register');
    }
}

function showLogin(pushState = true) {
    const loginCard = document.getElementById('loginCard');
    const registerCard = document.getElementById('registerCard');

    registerCard.classList.remove('active');
    registerCard.classList.add('exit-left');

    loginCard.classList.remove('exit-left');
    loginCard.classList.add('active');

    if (pushState) {
        history.pushState({}, '', window.location.pathname);
    }
}

async function handleLogin() {
    const login = document.getElementById('phone-login').value;
    const password = document.getElementById('password-login').value;

    // Валидация полей
    if (!login || !password) {
        showNotification('Пожалуйста, заполните все поля', 'warning');
        return;
    }

    try {
        const data = await api.post('/auth/login', {
            login,
            password
        });

        api.setToken(data.token);
        
        localStorage.setItem('userName', data.login);
        localStorage.setItem('userLogin', data.login);
        localStorage.setItem('userRole', data.role);

        showNotification(`Добро пожаловать, ${data.login}!`, 'success');
        
        setTimeout(() => {
            window.location.href = '/home.html';
        }, 500);

    } catch (error) {
        console.error('Login error:', error);
        
        // Обработка разных типов ошибок
        if (error.message === 'HTTP 401' || error.message.includes('401')) {
            showNotification('Неверный логин или пароль. Попробуйте снова.', 'error');
        } else if (error.message === 'HTTP 403') {
            showNotification('Доступ запрещен. Обратитесь к администратору.', 'error');
        } else if (error.message === 'HTTP 500') {
            showNotification('Ошибка сервера. Попробуйте позже.', 'error');
        } else if (error.message && error.message.includes('Неверный')) {
            showNotification(error.message, 'error');
        } else {
            showNotification('Ошибка подключения к серверу. Проверьте соединение.', 'error');
        }
    }
}

async function handleRegister() {
    const name = document.getElementById('name').value;
    const login = document.getElementById('phone-reg').value;
    const password = document.getElementById('password-reg').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    // Валидация полей
    if (!name || !login || !password) {
        showNotification('Пожалуйста, заполните все обязательные поля', 'warning');
        return;
    }

    if (password !== confirmPassword) {
        showNotification('Пароли не совпадают', 'error');
        return;
    }

    if (password.length < 4) {
        showNotification('Пароль должен содержать минимум 4 символа', 'warning');
        return;
    }

    try {
        const nameParts = name.trim().split(' ');
        let surname = '';
        let firstName = '';
        let patronymic = '';
        
        if (nameParts.length === 1) {
            firstName = nameParts[0];
        } else if (nameParts.length === 2) {
            surname = nameParts[0];
            firstName = nameParts[1];
        } else if (nameParts.length >= 3) {
            surname = nameParts[0];
            firstName = nameParts[1];
            patronymic = nameParts.slice(2).join(' ');
        }
        
        const data = await api.post('/auth/register', {
            surname: surname,
            name: firstName,
            patronymic: patronymic,
            login: login,
            password: password,
            roleId: null,
            buildingId: null,
            divisionId: null,
            postId: null
        });

        api.setToken(data.token);
        localStorage.setItem('userName', data.login || login);
        localStorage.setItem('userLogin', data.login || login);

        showNotification('Регистрация успешно завершена!', 'success');
        
        setTimeout(() => {
            window.location.href = '/home.html';
        }, 500);

    } catch (error) {
        console.error('Registration error:', error);
        
        if (error.message && error.message.includes('уже существует')) {
            showNotification('Пользователь с таким логином уже существует', 'error');
        } else if (error.message === 'HTTP 400') {
            showNotification('Некорректные данные. Проверьте введенную информацию.', 'error');
        } else if (error.message === 'HTTP 500') {
            showNotification('Ошибка сервера. Попробуйте позже.', 'error');
        } else {
            showNotification(error.message || 'Ошибка регистрации. Попробуйте позже.', 'error');
        }
    }
}

window.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.getElementById('loginBtn');
    const registerBtn = document.getElementById('registerBtn');
    const goRegister = document.getElementById('goRegister');
    const goLogin = document.getElementById('goLogin');

    if (loginBtn) loginBtn.addEventListener('click', handleLogin);
    if (registerBtn) registerBtn.addEventListener('click', handleRegister);
    if (goRegister) goRegister.addEventListener('click', () => showRegister(true));
    if (goLogin) goLogin.addEventListener('click', () => showLogin(true));

    const params = new URLSearchParams(window.location.search);
    if (params.get('form') === 'register') {
        showRegister(false);
    }
    
    // Добавляем обработку Enter
    const loginInput = document.getElementById('phone-login');
    const passwordInput = document.getElementById('password-login');
    
    if (loginInput) {
        loginInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleLogin();
        });
    }
    
    if (passwordInput) {
        passwordInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') handleLogin();
        });
    }
});

window.addEventListener('popstate', () => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('form') === 'register') {
        showRegister(false);
    } else {
        showLogin(false);
    }
});

let tempRegisterData = {};

function switchToRegister() {
    const loginCard = document.getElementById("loginCard");
    const registerCard = document.getElementById("registerCard");

    loginCard.classList.remove("active");
    loginCard.classList.add("exit-left");

    registerCard.classList.remove("enter-right");
    registerCard.classList.add("active");

    setTimeout(() => {
        loginCard.classList.remove("exit-left");
        loginCard.classList.add("enter-right");
    }, 500);
}

function switchToLogin() {
    const loginCard = document.getElementById("loginCard");
    const registerCard = document.getElementById("registerCard");
    const registerStep2 = document.getElementById("registerStep2");

    registerCard.classList.remove("active");
    registerStep2.classList.remove("active");
    registerCard.classList.add("exit-left");

    loginCard.classList.remove("enter-right");
    loginCard.classList.add("active");

    setTimeout(() => {
        registerCard.classList.remove("exit-left");
        registerCard.classList.add("enter-right");
    }, 500);
}

async function goToStep2() {
    const name = document.getElementById("name").value.trim();
    const login = document.getElementById("login").value.trim();
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (!name || !login || !password) {
        showNotification("Заполните все поля", "warning");
        return;
    }

    if (password !== confirmPassword) {
        showNotification("Пароли не совпадают", "error");
        return;
    }

    if (password.length < 4) {
        showNotification("Пароль должен содержать минимум 4 символа", "warning");
        return;
    }

    tempRegisterData = { name, login, password };

    const step1 = document.getElementById("registerCard");
    const step2 = document.getElementById("registerStep2");

    step1.classList.remove("active");
    step1.classList.add("exit-left");

    step2.classList.remove("enter-right");
    step2.classList.add("active");

    setTimeout(() => {
        step1.classList.remove("exit-left");
        step1.classList.add("enter-right");
    }, 500);

    await loadDropdowns();
}

function backToStep1() {
    const step1 = document.getElementById("registerCard");
    const step2 = document.getElementById("registerStep2");

    step2.classList.remove("active");
    step2.classList.add("exit-left");

    step1.classList.remove("enter-right");
    step1.classList.add("active");

    setTimeout(() => {
        step2.classList.remove("exit-left");
        step2.classList.add("enter-right");
    }, 500);
}

async function loadDropdowns() {
    try {
        const roles = await api.get("/api/roles");
        const buildings = await api.get("/api/buildings");
        const divisions = await api.get("/api/divisions");
        const posts = await api.get("/api/posts");

        fillSelect("roleSelect", roles, "id", "nameRole");
        fillSelect("buildingSelect", buildings, "id", "nameBuilding");
        fillSelect("divisionSelect", divisions, "id", "nameDivision");
        fillSelect("postSelect", posts, "id", "namePost");
    } catch (error) {
        console.error('Error loading dropdowns:', error);
        showNotification('Не удалось загрузить данные для формы', 'error');
    }
}

function fillSelect(id, data, valueField, textField) {
    const select = document.getElementById(id);
    if (!select) return;
    
    select.innerHTML = '<option value="">Не выбрано</option>';

    data.forEach(item => {
        const option = document.createElement("option");
        option.value = item[valueField];
        option.textContent = item[textField];
        select.appendChild(option);
    });
}

async function finishRegistration() {
    try {
        const roleSelect = document.getElementById("roleSelect");
        const buildingSelect = document.getElementById("buildingSelect");
        const divisionSelect = document.getElementById("divisionSelect");
        const postSelect = document.getElementById("postSelect");
        
        if (!tempRegisterData) {
            showNotification('Данные регистрации не найдены', 'error');
            return;
        }
        
        const fullName = tempRegisterData.name.trim();
        const nameParts = fullName.split(' ');
        
        let surname = '';
        let name = '';
        let patronymic = '';
        
        if (nameParts.length === 1) {
            name = nameParts[0];
        } else if (nameParts.length === 2) {
            surname = nameParts[0];
            name = nameParts[1];
        } else if (nameParts.length >= 3) {
            surname = nameParts[0];
            name = nameParts[1];
            patronymic = nameParts.slice(2).join(' ');
        }
        
        const requestData = {
            surname: surname,
            name: name,
            patronymic: patronymic,
            login: tempRegisterData.login,
            password: tempRegisterData.password,
            roleId: roleSelect && roleSelect.value ? Number(roleSelect.value) : null,
            buildingId: buildingSelect && buildingSelect.value ? Number(buildingSelect.value) : null,
            divisionId: divisionSelect && divisionSelect.value ? Number(divisionSelect.value) : null,
            postId: postSelect && postSelect.value ? Number(postSelect.value) : null
        };
        
        const response = await api.post("/auth/register", requestData);
        
        if (response && response.token) {
            api.setToken(response.token);
            localStorage.setItem('userName', response.login || tempRegisterData.name);
            localStorage.setItem('userLogin', response.login || tempRegisterData.login);
            
            showNotification('Регистрация успешно завершена!', 'success');
            
            setTimeout(() => {
                window.location.href = "/home.html";
            }, 500);
        } else {
            throw new Error('Не получен токен авторизации');
        }
        
    } catch (error) {
        console.error('Registration error:', error);
        
        if (error.message && error.message.includes('уже существует')) {
            showNotification('Пользователь с таким логином уже существует', 'error');
        } else {
            showNotification(error.message || 'Ошибка регистрации. Попробуйте позже.', 'error');
        }
    }
}

async function login() {
    try {
        const loginValue = document.getElementById("loginAuth").value;
        const passwordValue = document.getElementById("passwordAuth").value;
        
        if (!loginValue || !passwordValue) {
            showNotification('Заполните все поля', 'warning');
            return;
        }
        
        const response = await api.post("/auth/login", {
            login: loginValue,
            password: passwordValue
        });
        
        if (response && response.token) {
            api.setToken(response.token);
            localStorage.setItem('userName', response.login || loginValue);
            localStorage.setItem('userLogin', response.login || loginValue);
            
            showNotification('Вход выполнен успешно!', 'success');
            
            setTimeout(() => {
                window.location.href = "/home.html";
            }, 500);
        } else {
            throw new Error('Не получен токен авторизации');
        }
        
    } catch (error) {
        console.error('Login error:', error);
        
        if (error.message === 'HTTP 401' || error.message.includes('401')) {
            showNotification('Неверный логин или пароль', 'error');
        } else if (error.message === 'HTTP 403') {
            showNotification('Доступ запрещен', 'error');
        } else {
            showNotification(error.message || 'Ошибка входа. Попробуйте позже.', 'error');
        }
    }
}