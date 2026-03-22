const API_BASE_URL = 'http://localhost:58080';

class ApiClient {
    constructor() {
        // больше не храним this.token здесь
    }

    setToken(token) {
        localStorage.setItem('token', token);
    }

    clearToken() {
        localStorage.removeItem('token');
    }

    getToken() {
        return localStorage.getItem('token');
    }

    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;

        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
            console.log(`[API] → Authorization: Bearer ... добавлен для ${endpoint}`);
        } else {
            console.warn(`[API] Токен НЕ НАЙДЕН для ${endpoint}`);
        }

        const config = {
            ...options,
            headers,
            credentials: 'include'
        };

        const response = await fetch(url, config);

        if (response.status === 401) {
            this.clearToken();
            window.location.href = '/login.html';
            throw new Error('Unauthorized');
        }

        if (response.status === 204) return null;

        let data;
        try {
            data = await response.json();
        } catch {
            data = {};
        }

        if (!response.ok) {
            const errMsg = data.message || data.error || `HTTP ${response.status}`;
            throw new Error(errMsg);
        }

        return data;
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
        return this.request(endpoint, { method: 'DELETE' });
    }
}

const api = new ApiClient();