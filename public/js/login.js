async function handleLogin(event) {
    event.preventDefault();

    const login = document.getElementById('phone-login').value;
    const password = document.getElementById('password-login').value;

    try {
        const data = await api.post('/auth/login', {
            login: login,
            password: password
        });

        api.setToken(data.token);

        localStorage.setItem('userName', data.name);
        localStorage.setItem('userLogin', data.login);

        window.location.href = '/home.html';

    } catch (error) {
        alert(error.message || 'Ошибка входа');
    }
}

async function handleRegister(event) {
    event.preventDefault();

    const name = document.getElementById('name').value;
    const login = document.getElementById('phone-reg').value; // ← логин
    const password = document.getElementById('password-reg').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (password !== confirmPassword) {
        alert('Пароли не совпадают');
        return;
    }

    try {
        const data = await api.post('/auth/register', {
            name: name,
            login: login,
            password: password
        });

        api.setToken(data.token);

        localStorage.setItem('userName', data.name);
        localStorage.setItem('userLogin', data.login); // сохраняем логин

        window.location.href = '/home.html';

    } catch (error) {
        alert(error.message || 'Ошибка регистрации');
    }
}

// Добавляем обработчики кнопок
window.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.querySelector('#loginCard .submit-btn');
    const registerBtn = document.querySelector('#registerCard .submit-btn');

    loginBtn.addEventListener('click', handleLogin);
    registerBtn.addEventListener('click', handleRegister);
});