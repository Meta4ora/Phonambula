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

    try {
        const data = await api.post('/auth/login', {
            login,
            password
        });

        api.setToken(data.token);

        localStorage.setItem('userName', data.name);
        localStorage.setItem('userLogin', data.login);

        window.location.href = '/home.html';

    } catch (error) {
        alert(error.message || 'Ошибка входа');
    }
}

async function handleRegister() {
    const name = document.getElementById('name').value;
    const login = document.getElementById('phone-reg').value;
    const password = document.getElementById('password-reg').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (!name || !login || !password) {
        alert('Заполните все поля');
        return;
    }

    if (password !== confirmPassword) {
        alert('Пароли не совпадают');
        return;
    }

    try {
        // Разделяем имя на части
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
            roleId: null,  // По умолчанию без роли
            buildingId: null,
            divisionId: null,
            postId: null
        });

        api.setToken(data.token);
        localStorage.setItem('userName', data.login || login);
        localStorage.setItem('userLogin', data.login || login);

        window.location.href = '/home.html';

    } catch (error) {
        console.error('Registration error:', error);
        alert(error.message || 'Ошибка регистрации');
    }
}

window.addEventListener('DOMContentLoaded', () => {

    // Кнопки
    document.getElementById('loginBtn')
        .addEventListener('click', handleLogin);

    document.getElementById('registerBtn')
        .addEventListener('click', handleRegister);

    // Переключатели
    document.getElementById('goRegister')
        .addEventListener('click', () => showRegister(true));

    document.getElementById('goLogin')
        .addEventListener('click', () => showLogin(true));

    // Проверка URL при загрузке
    const params = new URLSearchParams(window.location.search);
    if (params.get('form') === 'register') {
        showRegister(false);
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
        alert("Заполните все поля");
        return;
    }

    if (password !== confirmPassword) {
        alert("Пароли не совпадают");
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
    const roles = await api.get("/api/roles");
    const buildings = await api.get("/api/buildings");
    const divisions = await api.get("/api/divisions");
    const posts = await api.get("/api/posts");

    fillSelect("roleSelect", roles, "id", "nameRole");
    fillSelect("buildingSelect", buildings, "id", "nameBuilding");
    fillSelect("divisionSelect", divisions, "id", "nameDivision");
    fillSelect("postSelect", posts, "id", "namePost");
}

function fillSelect(id, data, valueField, textField) {
    const select = document.getElementById(id);
    select.innerHTML = "";

    data.forEach(item => {
        const option = document.createElement("option");
        option.value = item[valueField];
        option.textContent = item[textField];
        select.appendChild(option);
    });
}

async function finishRegistration() {
    try {
        // Получаем значения из select
        const roleSelect = document.getElementById("roleSelect");
        const buildingSelect = document.getElementById("buildingSelect");
        const divisionSelect = document.getElementById("divisionSelect");
        const postSelect = document.getElementById("postSelect");
        
        // Разделяем ФИО на части
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
            roleId: roleSelect ? Number(roleSelect.value) : null,
            buildingId: buildingSelect ? Number(buildingSelect.value) : null,
            divisionId: divisionSelect ? Number(divisionSelect.value) : null,
            postId: postSelect ? Number(postSelect.value) : null
        };
        
        console.log('Sending registration data:', requestData);
        
        const response = await api.post("/auth/register", requestData);
        
        if (response && response.token) {
            api.setToken(response.token);
            localStorage.setItem('userName', response.login || tempRegisterData.name);
            localStorage.setItem('userLogin', response.login || tempRegisterData.login);
            window.location.href = "/home.html";
        } else {
            throw new Error('Не получен токен авторизации');
        }
        
    } catch (error) {
        console.error('Registration error:', error);
        alert('Ошибка регистрации: ' + (error.message || 'Попробуйте позже'));
    }
}

async function login() {
    try {
        const loginValue = document.getElementById("loginAuth").value;
        const passwordValue = document.getElementById("passwordAuth").value;
        
        if (!loginValue || !passwordValue) {
            alert('Заполните все поля');
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
            window.location.href = "/home.html";
        } else {
            throw new Error('Не получен токен авторизации');
        }
        
    } catch (error) {
        console.error('Login error:', error);
        alert('Ошибка входа: ' + (error.message || 'Неверный логин или пароль'));
    }
}