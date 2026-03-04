// Переключение форм
function showCard(showId, hideId) {
    const showCard = document.getElementById(showId);
    const hideCard = document.getElementById(hideId);

    // Сначала добавляем класс ухода текущей карточке
    hideCard.classList.add('exit-left');
    
    // Через маленький таймаут запускаем появление новой
    setTimeout(() => {
        hideCard.classList.remove('active', 'exit-left');
        showCard.classList.add('active', 'enter-right');

        // Убираем enter-right после завершения анимации
        setTimeout(() => {
            showCard.classList.remove('enter-right');
        }, 500); // должно совпадать с длительностью transition в CSS
    }, 100);
}

document.addEventListener('DOMContentLoaded', () => {
    // Переключение на регистрацию
    document.getElementById('switch-to-register').addEventListener('click', () => {
        showCard('registerCard', 'loginCard');
    });

    // Переключение на вход
    document.getElementById('switch-to-login').addEventListener('click', () => {
        showCard('loginCard', 'registerCard');
    });

    // Обработчики отправки форм (твой код остаётся)
    const loginBtn = document.querySelector('#loginCard .submit-btn');
    const registerBtn = document.querySelector('#registerCard .submit-btn');

    loginBtn.addEventListener('click', handleLogin);
    registerBtn.addEventListener('click', handleRegister);
});


// ────────────────────────────────────────────────
// Твой существующий код обработки форм (оставляем без изменений)

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
    const login = document.getElementById('phone-reg').value;
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
        localStorage.setItem('userLogin', data.login);

        window.location.href = '/home.html';

    } catch (error) {
        alert(error.message || 'Ошибка регистрации');
    }
}