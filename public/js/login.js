// login.js — полный исправленный вариант

const api = new ApiClient(); // предполагаем, что ApiClient уже определён в api.js

// ---------------------- Переключение форм ----------------------

function showRegister(pushState = true) {
    const loginCard    = document.getElementById('loginCard');
    const registerCard = document.getElementById('registerCard');

    if (!loginCard || !registerCard) return;

    loginCard.classList.remove('active');
    loginCard.classList.add('exit-left');

    registerCard.classList.remove('exit-left');
    registerCard.classList.add('active');

    if (pushState) {
        history.pushState({ form: 'register' }, '', '?form=register');
    }
}

function showLogin(pushState = true) {
    const loginCard    = document.getElementById('loginCard');
    const registerCard = document.getElementById('registerCard');

    if (!loginCard || !registerCard) return;

    registerCard.classList.remove('active');
    registerCard.classList.add('exit-left');

    loginCard.classList.remove('exit-left');
    loginCard.classList.add('active');

    if (pushState) {
        history.pushState({}, '', window.location.pathname);
    }
}

// ---------------------- Обработчики входа и регистрации ----------------------

async function handleLogin() {
    const phoneInput = document.getElementById('phone-login');
    const passInput  = document.getElementById('password-login');

    if (!phoneInput || !passInput) {
        alert('Ошибка интерфейса — поля не найдены');
        return;
    }

    const login    = phoneInput.value.trim();
    const password = passInput.value.trim();

    if (!login || !password) {
        alert('Введите номер телефона и пароль');
        return;
    }

    try {
        const data = await api.post('/auth/login', { login, password });

        if (!data?.token) {
            throw new Error('Сервер не вернул токен авторизации');
        }

        localStorage.setItem('token', data.token);
        api.setToken(data.token);

        localStorage.setItem('userName',  data.name  || data.login || 'Пользователь');
        localStorage.setItem('userLogin', data.login || '');

        console.log("[LOGIN] Токен сохранён, длина:", data.token.length);

        window.location.href = '/home.html';
    } catch (err) {
        console.error("[LOGIN] Ошибка:", err);
        alert(err.message || 'Не удалось войти. Проверьте данные.');
    }
}

async function handleRegister() {
    // Это старая версия — если вы её используете, замените на finishRegistration
    // Рекомендую использовать многошаговую регистрацию (goToStep2 → finishRegistration)
    alert('Используйте многошаговую регистрацию (finishRegistration)');
}

// ---------------------- Многошаговая регистрация ----------------------

let tempRegisterData = {};

async function goToStep2() {
    const nameEl     = document.getElementById("name");
    const loginEl    = document.getElementById("login");
    const passEl     = document.getElementById("password");
    const confirmEl  = document.getElementById("confirmPassword");

    if (!nameEl || !loginEl || !passEl || !confirmEl) {
        alert("Ошибка — некоторые поля не найдены");
        return;
    }

    const name     = nameEl.value.trim();
    const login    = loginEl.value.trim();
    const password = passEl.value.trim();
    const confirm  = confirmEl.value.trim();

    if (!name || !login || !password) {
        alert("Заполните все обязательные поля");
        return;
    }

    if (password !== confirm) {
        alert("Пароли не совпадают");
        return;
    }

    tempRegisterData = { name, login, password };

    const step1 = document.getElementById("registerCard");
    const step2 = document.getElementById("registerStep2");

    if (!step1 || !step2) return;

    step1.classList.remove("active");
    step1.classList.add("exit-left");

    step2.classList.remove("enter-right");
    step2.classList.add("active");

    setTimeout(() => {
        step1.classList.remove("exit-left");
        step1.classList.add("enter-right");
    }, 500);

    try {
        await loadDropdowns();
    } catch (err) {
        console.error("Не удалось загрузить справочники:", err);
        alert("Не удалось загрузить списки ролей/отделов и т.д.");
    }
}

function backToStep1() {
    const step1 = document.getElementById("registerCard");
    const step2 = document.getElementById("registerStep2");

    if (!step1 || !step2) return;

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
        const [roles, buildings, divisions, posts] = await Promise.all([
            api.get("/api/roles"),
            api.get("/api/buildings"),
            api.get("/api/divisions"),
            api.get("/api/posts")
        ]);

        fillSelect("roleSelect",     roles,     "id", "nameRole");
        fillSelect("buildingSelect", buildings, "id", "nameBuilding");
        fillSelect("divisionSelect", divisions, "id", "nameDivision");
        fillSelect("postSelect",     posts,     "id", "namePost");
    } catch (err) {
        console.error("Ошибка загрузки справочников:", err);
        throw err;
    }
}

function fillSelect(id, data, valueField, textField) {
    const select = document.getElementById(id);
    if (!select) return;

    select.innerHTML = '<option value="">— Выберите —</option>';

    if (!Array.isArray(data)) return;

    data.forEach(item => {
        const option = document.createElement("option");
        option.value = item[valueField];
        option.textContent = item[textField] || item.name || '—';
        select.appendChild(option);
    });
}

async function finishRegistration() {
    const roleEl     = document.getElementById("roleSelect");
    const buildingEl = document.getElementById("buildingSelect");
    const divisionEl = document.getElementById("divisionSelect");
    const postEl     = document.getElementById("postSelect");

    if (!roleEl || !buildingEl || !divisionEl || !postEl) {
        alert("Ошибка — не найдены поля выбора");
        return;
    }

    const payload = {
        surname:    "",
        name:       tempRegisterData.name     || "",
        patronymic: "",
        login:      tempRegisterData.login    || "",
        password:   tempRegisterData.password || "",

        roleId:     Number(roleEl.value)     || null,
        buildingId: Number(buildingEl.value) || null,
        divisionId: Number(divisionEl.value) || null,
        postId:     Number(postEl.value)     || null
    };

    if (!payload.login || !payload.password) {
        alert("Критическая ошибка — логин или пароль потерялись");
        return;
    }

    try {
        const response = await api.post("/auth/register", payload);

        if (!response?.token) {
            throw new Error("Регистрация прошла, но токен не получен");
        }

        localStorage.setItem("token", response.token);
        api.setToken(response.token);

        console.log("[REGISTER] Токен сохранён, длина:", response.token.length);

        window.location.href = "/home.html";
    } catch (err) {
        console.error("[REGISTER] Ошибка:", err);
        alert(err.message || "Не удалось завершить регистрацию");
    }
}

// ---------------------- Инициализация ----------------------

window.addEventListener('DOMContentLoaded', () => {
    // Кнопки переключения форм
    const goRegisterBtn = document.getElementById('goRegister');
    const goLoginBtn    = document.getElementById('goLogin');

    if (goRegisterBtn) goRegisterBtn.addEventListener('click', () => showRegister(true));
    if (goLoginBtn)    goLoginBtn.addEventListener('click',    () => showLogin(true));

    // Кнопки отправки
    const loginBtn = document.getElementById('loginBtn');
    if (loginBtn) loginBtn.addEventListener('click', handleLogin);

    // Многошаговая регистрация — предполагаем, что есть кнопки "Далее" и "Зарегистрироваться"
    const nextBtn = document.getElementById('nextStepBtn'); // дайте id вашей кнопке "Далее"
    if (nextBtn) nextBtn.addEventListener('click', goToStep2);

    const finishBtn = document.getElementById('finishRegisterBtn'); // кнопка "Завершить"
    if (finishBtn) finishBtn.addEventListener('click', finishRegistration);

    const backBtn = document.getElementById('backToStep1');
    if (backBtn) backBtn.addEventListener('click', backToStep1);

    // Проверка URL
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