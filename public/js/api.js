const API_BASE_URL = 'http://localhost:58080';

class ApiClient {
    constructor() {
        this.token = localStorage.getItem('token');
    }

    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
        }
    }

    clearToken() {
        this.token = null;
        localStorage.removeItem('token');
    }

    isAuthenticated() {
        return !!this.token;
    }

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const config = {
            ...options,
            headers,
            credentials: 'include'
        };

        try {
            const response = await fetch(url, config);
            
            // Обработка 401 Unauthorized
            if (response.status === 401) {
                this.clearToken();
                // Не перенаправляем на логин, если это не запрос на авторизацию
                if (!endpoint.includes('/auth/')) {
                    window.location.href = '/login.html';
                }
                throw new Error('Не авторизован');
            }
            
            // Проверяем, есть ли тело ответа
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                const data = await response.json();
                
                if (!response.ok) {
                    throw new Error(data.message || data.error || `HTTP ${response.status}`);
                }
                
                return data;
            }
            
            // Для ответов без тела
            if (response.status === 204) {
                return null;
            }
            
            // Для текстовых ответов
            const text = await response.text();
            if (!response.ok) {
                throw new Error(text || `HTTP ${response.status}`);
            }
            
            return text;
            
        } catch (error) {
            console.error(`API request failed (${endpoint}):`, error);
            throw error;
        }
    }

    async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    async post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    async put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    }

    async delete(endpoint) {
        return this.request(endpoint, {
            method: 'DELETE'
        });
    }
}

const api = new ApiClient();