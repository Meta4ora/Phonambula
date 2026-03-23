let contactsData = {};
let editedContactId = null;
let currentUserRole = null;
let searchTerm = '';
let currentAuditPage = 1;
let auditTotalPages = 1;
let auditData = [];

// Функция для экранирования HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Функция для проверки, является ли пользователь администратором
function isAdmin() {
    if (!currentUserRole) return false;
    const roleLower = currentUserRole.toLowerCase();
    return roleLower === 'администратор' || 
           roleLower === 'administrator' || 
           roleLower === 'admin';
}

// Функция для показа уведомлений
function showNotification(message, type = 'success') {
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }
    
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 24px;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        z-index: 10000;
        animation: slideIn 0.3s ease;
        background: ${type === 'success' ? '#4CAF50' : type === 'error' ? '#f44336' : '#ff9800'};
        color: white;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Добавляем стили для анимации
if (!document.querySelector('#animation-styles')) {
    const style = document.createElement('style');
    style.id = 'animation-styles';
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(100%);
                opacity: 0;
            }
        }
        .highlight {
            background-color: rgba(77, 63, 141, 0.2);
            border-radius: 3px;
            padding: 0 2px;
        }
    `;
    document.head.appendChild(style);
}

// Функция для обработки ошибок API
async function safeApiCall(apiCall, errorMessage = 'Произошла ошибка') {
    try {
        return await apiCall();
    } catch (err) {
        console.error('API Error:', err);
        
        if (err.message === 'HTTP 401' || err.message === 'HTTP 403') {
            showNotification('Сессия истекла. Перенаправление на страницу входа...', 'error');
            setTimeout(() => {
                api.clearToken();
                localStorage.clear();
                window.location.href = '/login.html';
            }, 1500);
            throw err;
        }
        
        showNotification(errorMessage + ': ' + (err.message || 'Неизвестная ошибка'), 'error');
        throw err;
    }
}

// Функция поиска контактов (возвращает ID контактов, подходящих под поиск)
function getFilteredContactIds(term) {
    if (!term || term.trim() === '') {
        return Object.keys(contactsData);
    }
    
    const searchLower = term.toLowerCase().trim();
    
    return Object.keys(contactsData).filter(id => {
        const contact = contactsData[id];
        return (
            (contact.name && contact.name.toLowerCase().includes(searchLower)) ||
            (contact.mobilePhone && contact.mobilePhone.toLowerCase().includes(searchLower)) ||
            (contact.landlinePhone && contact.landlinePhone.toLowerCase().includes(searchLower)) ||
            (contact.internalPhone && contact.internalPhone.toLowerCase().includes(searchLower)) ||
            (contact.position && contact.position.toLowerCase().includes(searchLower)) ||
            (contact.department && contact.department.toLowerCase().includes(searchLower)) ||
            (contact.building && contact.building.toLowerCase().includes(searchLower)) ||
            (contact.cabinet && contact.cabinet.toLowerCase().includes(searchLower)) ||
            (contact.id && contact.id.toString().includes(searchLower))
        );
    });
}

// Функция для подсветки найденного текста
function highlightText(text, searchTerm) {
    if (!searchTerm || !text || searchTerm.trim() === '') return escapeHtml(text);
    
    const searchLower = searchTerm.toLowerCase();
    const textLower = text.toLowerCase();
    const index = textLower.indexOf(searchLower);
    
    if (index === -1) return escapeHtml(text);
    
    const start = text.substring(0, index);
    const match = text.substring(index, index + searchTerm.length);
    const end = text.substring(index + searchTerm.length);
    
    return `${escapeHtml(start)}<span class="highlight">${escapeHtml(match)}</span>${escapeHtml(end)}`;
}

// Функция построения дерева контактов
function rebuildContactTree() {
    const treeContainer = document.querySelector('.contact-tree');
    if (!treeContainer) return;

    const filteredIds = getFilteredContactIds(searchTerm);
    const filteredContacts = filteredIds.map(id => contactsData[id]);
    
    treeContainer.innerHTML = '';

    if (filteredContacts.length === 0) {
        treeContainer.innerHTML = '<li><p>Нет контактов</p></li>';
        const searchContainer = document.querySelector('.search-container');
        const existingCount = document.querySelector('.search-result-count');
        if (existingCount) existingCount.remove();
        
        const countDiv = document.createElement('div');
        countDiv.className = 'search-result-count';
        countDiv.textContent = searchTerm ? 'Ничего не найдено' : 'Нет контактов';
        if (searchContainer) {
            searchContainer.insertAdjacentElement('afterend', countDiv);
        }
        return;
    }

    // Группировка
    const grouped = {};
    filteredContacts.forEach(contact => {
        const firstLetter = (contact.name || '?')[0].toUpperCase();
        if (!grouped[firstLetter]) grouped[firstLetter] = [];
        grouped[firstLetter].push(contact);
    });

    let html = '';
    Object.keys(grouped).sort().forEach(letter => {
        html += `
            <li>
                <div class="category expanded" onclick="toggleCategory(this)">${escapeHtml(letter)}</div>
                <ul class="sub-list">
        `;

        grouped[letter].forEach(contact => {
            const deleteButton = isAdmin() 
                ? `<button class="delete-btn" onclick="deleteContact(event,'${contact.id}')">Удалить</button>`
                : '';
                
            const displayName = searchTerm ? highlightText(contact.name || '—', searchTerm) : escapeHtml(contact.name || '—');
                
            html += `
                <li class="contact-item" data-contact-id="${contact.id}" onclick="selectContact(this,'${contact.id}')">
                    ${displayName}
                    ${deleteButton}
                </li>
            `;
        });

        html += `
                </ul>
            </li>
        `;
    });

    treeContainer.innerHTML = html;
    
    // Добавляем счетчик
    const searchContainer = document.querySelector('.search-container');
    const existingCount = document.querySelector('.search-result-count');
    if (existingCount) existingCount.remove();
    
    const countDiv = document.createElement('div');
    countDiv.className = 'search-result-count';
    if (searchTerm) {
        countDiv.textContent = `Найдено: ${filteredContacts.length} из ${Object.keys(contactsData).length}`;
    } else {
        countDiv.textContent = `Всего контактов: ${Object.keys(contactsData).length}`;
    }
    if (searchContainer) {
        searchContainer.insertAdjacentElement('afterend', countDiv);
    }
    
    // Восстанавливаем активный класс для выбранного контакта
    if (editedContactId && filteredIds.includes(editedContactId.toString())) {
        const activeItem = document.querySelector(`.contact-item[data-contact-id="${editedContactId}"]`);
        if (activeItem) {
            activeItem.classList.add('active');
        }
    }
}

// Функция обработки поиска
function handleSearch() {
    const searchInput = document.getElementById('searchInput');
    const clearBtn = document.getElementById('clearSearchBtn');
    
    if (searchInput) {
        searchTerm = searchInput.value;
        
        if (clearBtn) {
            clearBtn.style.display = searchTerm ? 'flex' : 'none';
        }
        
        rebuildContactTree();
        
        // Проверяем, виден ли текущий выбранный контакт
        const filteredIds = getFilteredContactIds(searchTerm);
        if (editedContactId && filteredIds.includes(editedContactId.toString())) {
            // Если выбранный контакт виден, обновляем его детали (для подсветки)
            showContactDetails(editedContactId);
        } else if (filteredIds.length > 0) {
            // Если выбранный контакт не виден, выбираем первый из результатов
            const firstVisibleId = filteredIds[0];
            const firstVisibleElement = document.querySelector(`.contact-item[data-contact-id="${firstVisibleId}"]`);
            if (firstVisibleElement) {
                selectContact(firstVisibleElement, firstVisibleId);
            }
        } else {
            // Если нет результатов, очищаем детали
            const detailsDiv = document.getElementById('contactDetails');
            if (detailsDiv) {
                detailsDiv.innerHTML = '<div class="no-contact-selected"><p>Нет контактов для отображения</p></div>';
            }
        }
    }
}

// Функция очистки поиска
function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.value = '';
        handleSearch();
        searchInput.focus();
    }
}

// Загрузка контактов
async function loadMyContacts() {
    try {
        const data = await safeApiCall(
            () => api.get('/api/subscribers/my'),
            'Не удалось загрузить контакты'
        );
        
        contactsData = {};
        data.forEach(contact => {
            contactsData[contact.id] = contact;
        });
        
        rebuildContactTree();
        
        const filteredIds = getFilteredContactIds(searchTerm);
        if (filteredIds.length > 0) {
            // Если есть выбранный контакт и он виден, показываем его
            if (editedContactId && filteredIds.includes(editedContactId.toString())) {
                showContactDetails(editedContactId);
                // Восстанавливаем активный класс
                const activeItem = document.querySelector(`.contact-item[data-contact-id="${editedContactId}"]`);
                if (activeItem) {
                    document.querySelectorAll('.contact-item').forEach(i => i.classList.remove('active'));
                    activeItem.classList.add('active');
                }
            } else {
                // Иначе показываем первый контакт
                const firstId = filteredIds[0];
                const firstElement = document.querySelector(`.contact-item[data-contact-id="${firstId}"]`);
                if (firstElement) {
                    selectContact(firstElement, firstId);
                } else {
                    showContactDetails(firstId);
                }
            }
        } else {
            const detailsDiv = document.getElementById('contactDetails');
            if (detailsDiv) {
                detailsDiv.innerHTML = '<div class="no-contact-selected"><p>Нет контактов для отображения</p></div>';
            }
        }
        
        const addEmployeeBtn = document.getElementById('addEmployeeBtn');
        if (addEmployeeBtn) {
            addEmployeeBtn.style.display = isAdmin() ? 'block' : 'none';
        }

        // Показываем или скрываем кнопку аудита
        const auditBtn = document.getElementById('viewAuditBtn');
        if (auditBtn) {
            auditBtn.style.display = isAdmin() ? 'flex' : 'none';
        }
        
    } catch (err) {
        console.error("Ошибка загрузки контактов:", err);
    }
}

// Выбор контакта
function selectContact(element, contactId) {
    // Проверяем, существует ли контакт
    if (!contactsData[contactId]) {
        showNotification('Контакт не найден', 'error');
        return;
    }
    
    // Если есть активный поиск, проверяем, виден ли контакт
    if (searchTerm && searchTerm.trim() !== '') {
        const filteredIds = getFilteredContactIds(searchTerm);
        if (!filteredIds.includes(contactId.toString())) {
            showNotification('Этот контакт не отображается в результатах поиска', 'warning');
            return;
        }
    }
    
    // Обновляем активный класс
    document.querySelectorAll('.contact-item').forEach(i => i.classList.remove('active'));
    element.classList.add('active');
    
    // Показываем детали
    showContactDetails(contactId);
}

// Показ деталей контакта
function showContactDetails(contactId) {
    if (!contactsData[contactId]) {
        const detailsDiv = document.getElementById('contactDetails');
        if (detailsDiv) {
            detailsDiv.innerHTML = '<div class="no-contact-selected"><p>Контакт не найден</p></div>';
        }
        return;
    }
    
    const c = contactsData[contactId];
    editedContactId = contactId;
    
    const detailsDiv = document.getElementById('contactDetails');
    if (!detailsDiv) return;

    const showEditButtons = isAdmin();
    const editButtonHtml = (field, id, name) => showEditButtons 
        ? `<button class="edit-btn" onclick="editReference('${field}', ${id || 'null'}, '${escapeHtml(name || '').replace(/'/g,"\\'")}')">Изменить</button>`
        : '';

    const highlightField = (value) => {
        if (!searchTerm || !value) return escapeHtml(value || '—');
        if (value.toLowerCase().includes(searchTerm.toLowerCase())) {
            return highlightText(value, searchTerm);
        }
        return escapeHtml(value || '—');
    };

    detailsDiv.innerHTML = `
    <h2>${highlightField(c.name)}</h2>

    <div class="detail-section">
        <h4>Контактные данные</h4>

        <div class="detail-item">
            <div class="detail-label">Мобильный</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="mobilePhone"
                 data-original="${escapeHtml(c.mobilePhone || '')}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'mobilePhone')"` : ''}>
                ${highlightField(c.mobilePhone)}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Городской</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="landlinePhone"
                 data-original="${escapeHtml(c.landlinePhone || '')}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'landlinePhone')"` : ''}>
                ${highlightField(c.landlinePhone)}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Внутренний</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="internalPhone"
                 data-original="${escapeHtml(c.internalPhone || '')}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'internalPhone')"` : ''}>
                ${highlightField(c.internalPhone)}
            </div>
        </div>
    </div>

    <div class="detail-section">
        <h4>Информация</h4>

        <div class="detail-item">
            <div class="detail-label">Должность</div>
            <div class="value-edit-row">
                <div class="detail-value"
                     data-field="position"
                     data-id="${c.positionId || ''}">
                    ${highlightField(c.position)}
                </div>
                ${editButtonHtml('position', c.positionId, c.position)}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Отдел</div>
            <div class="value-edit-row">
                <div class="detail-value"
                     data-field="department"
                     data-id="${c.departmentId || ''}">
                    ${highlightField(c.department)}
                </div>
                ${editButtonHtml('department', c.departmentId, c.department)}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Корпус</div>
            <div class="value-edit-row">
                <div class="detail-value"
                     data-field="building"
                     data-id="${c.buildingId || ''}">
                    ${highlightField(c.building)}
                </div>
                ${editButtonHtml('building', c.buildingId, c.building)}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Кабинет</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="cabinet"
                 data-original="${escapeHtml(c.cabinet || '')}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'cabinet')"` : ''}>
                ${highlightField(c.cabinet)}
            </div>
        </div>
    </div>

    <div class="save-button-container">
        <button id="saveBtn" style="display:none;" onclick="saveContact()">Сохранить изменения</button>
    </div>
`;
}

// Функция удаления контакта
async function deleteContact(event, id) {
    event.stopPropagation();
    
    if (!isAdmin()) {
        showNotification('У вас нет прав для удаления контактов', 'error');
        return;
    }

    const contact = contactsData[id];
    if (!confirm(`Удалить контакт "${contact?.name || '?'}"?`)) return;

    try {
        await safeApiCall(
            () => api.delete(`/api/subscribers/${id}`),
            'Не удалось удалить контакт'
        );
        
        showNotification('Контакт успешно удалён', 'success');

        if (String(id) === String(editedContactId)) {
            editedContactId = null;
        }

        await loadMyContacts();
    } catch (err) {
        console.error(err);
    }
}

// Функция открытия модального окна добавления сотрудника
async function openAddEmployeeModal() {
    if (!isAdmin()) {
        showNotification('У вас нет прав для добавления сотрудников', 'error');
        return;
    }
    await loadReferenceDataForModal();
    const modal = document.getElementById('addEmployeeModal');
    if (modal) {
        modal.style.display = 'flex';
    }
}

// Функция закрытия модального окна
function closeAddEmployeeModal() {
    const modal = document.getElementById('addEmployeeModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Загрузка данных для модального окна
async function loadReferenceDataForModal() {
    try {
        const positions = await safeApiCall(() => api.get('/api/posts'), 'Не удалось загрузить должности');
        const positionSelect = document.getElementById('empPosition');
        if (positionSelect) {
            positionSelect.innerHTML = '<option value="">Не выбрано</option>';
            positions.forEach(pos => {
                const option = document.createElement('option');
                option.value = pos.id;
                option.textContent = pos.namePost;
                positionSelect.appendChild(option);
            });
        }

        const divisions = await safeApiCall(() => api.get('/api/divisions'), 'Не удалось загрузить отделы');
        const divisionSelect = document.getElementById('empDivision');
        if (divisionSelect) {
            divisionSelect.innerHTML = '<option value="">Не выбрано</option>';
            divisions.forEach(div => {
                const option = document.createElement('option');
                option.value = div.id;
                option.textContent = div.nameDivision;
                divisionSelect.appendChild(option);
            });
        }

        const buildings = await safeApiCall(() => api.get('/api/buildings'), 'Не удалось загрузить корпуса');
        const buildingSelect = document.getElementById('empBuilding');
        if (buildingSelect) {
            buildingSelect.innerHTML = '<option value="">Не выбрано</option>';
            buildings.forEach(build => {
                const option = document.createElement('option');
                option.value = build.id;
                option.textContent = build.nameBuilding || build.address || 'Без названия';
                buildingSelect.appendChild(option);
            });
        }

        const roles = await safeApiCall(() => api.get('/api/roles'), 'Не удалось загрузить роли');
        const roleSelect = document.getElementById('empRole');
        if (roleSelect) {
            roleSelect.innerHTML = '<option value="">Не выбрано</option>';
            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.id;
                option.textContent = role.nameRole || role.name;
                roleSelect.appendChild(option);
            });
        }

    } catch (err) {
        console.error('Ошибка загрузки справочников:', err);
    }
}

// Функция сохранения нового сотрудника
async function saveNewEmployee() {
    if (!isAdmin()) {
        showNotification('У вас нет прав для добавления сотрудников', 'error');
        return;
    }
    
    const password = document.getElementById('empPassword').value;
    const confirmPassword = document.getElementById('empConfirmPassword').value;
    
    if (password !== confirmPassword) {
        showNotification('Пароли не совпадают', 'error');
        return;
    }
    
    if (!password || password.length < 4) {
        showNotification('Пароль должен содержать минимум 4 символа', 'error');
        return;
    }
    
    const surname = document.getElementById('empSurname').value.trim();
    const name = document.getElementById('empName').value.trim();
    const patronymic = document.getElementById('empPatronymic').value.trim();
    const login = document.getElementById('empLogin').value.trim();
    
    if (!surname || !name || !login) {
        showNotification('Заполните все обязательные поля (Фамилия, Имя, Логин)', 'error');
        return;
    }
    
    const roleId = document.getElementById('empRole').value;
    const postId = document.getElementById('empPosition').value;
    const divisionId = document.getElementById('empDivision').value;
    const buildingId = document.getElementById('empBuilding').value;
    
    const data = {
        surname, name, patronymic: patronymic || "",
        login, password,
        roleId: roleId ? Number(roleId) : null,
        postId: postId ? Number(postId) : null,
        divisionId: divisionId ? Number(divisionId) : null,
        buildingId: buildingId ? Number(buildingId) : null,
        mobilePhoneNumber: document.getElementById('empMobilePhone').value.trim() || "",
        landlinePhoneNumber: document.getElementById('empLandlinePhone').value.trim() || "",
        internalPhoneNumber: document.getElementById('empInternalPhone').value.trim() || "",
        cabinetNumber: document.getElementById('empCabinet').value.trim() || ""
    };
    
    try {
        await safeApiCall(
            () => api.post('/auth/register', data),
            'Не удалось добавить сотрудника'
        );
        
        showNotification('Сотрудник успешно добавлен', 'success');
        closeAddEmployeeModal();
        
        document.querySelectorAll('#addEmployeeModal input').forEach(i => i.value = '');
        document.querySelectorAll('#addEmployeeModal select').forEach(s => s.value = '');
        
        await loadMyContacts();
    } catch (err) {
        console.error('Ошибка при добавлении:', err);
    }
}

// Функция экспорта в PDF
async function exportToPDF() {
    try {
        const exportBtn = document.getElementById('exportPdfBtn');
        const originalText = exportBtn.textContent;
        exportBtn.textContent = '⏳ Экспорт...';
        exportBtn.disabled = true;
        
        const contacts = Object.values(contactsData);
        
        if (contacts.length === 0) {
            showNotification('Нет контактов для экспорта', 'error');
            exportBtn.textContent = originalText;
            exportBtn.disabled = false;
            return;
        }
        
        const pdfDiv = document.getElementById('pdfContent');
        const currentDate = new Date().toLocaleString('ru-RU');
        
        let html = `
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Phonambula Contacts</title>
                <style>
                    body { font-family: 'Arial', 'Helvetica', sans-serif; padding: 20px; background: white; }
                    h1 { color: #dc3545; text-align: center; font-size: 24px; margin-bottom: 10px; }
                    .info { text-align: center; color: #666; margin-bottom: 20px; font-size: 12px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 10px; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; vertical-align: top; }
                    th { background-color: #dc3545; color: white; font-weight: bold; font-size: 11px; text-align: center; }
                    tr:nth-child(even) { background-color: #f9f9f9; }
                    .footer { text-align: center; margin-top: 20px; color: #999; font-size: 10px; }
                </style>
            </head>
            <body>
                <h1>Phonambula - Список контактов</h1>
                <div class="info">Дата генерации: ${escapeHtml(currentDate)}<br>Всего контактов: ${contacts.length}</div>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th><th>ФИО</th><th>Мобильный</th><th>Городской</th>
                            <th>Внутренний</th><th>Должность</th><th>Отдел</th><th>Корпус</th><th>Кабинет</th>
                        </tr>
                    </thead>
                    <tbody>
        `;
        
        contacts.forEach(contact => {
            html += `
                <tr>
                    <td style="text-align:center">${escapeHtml(contact.id || '')}</td>
                    <td>${escapeHtml(contact.name || '—')}</td>
                    <td>${escapeHtml(contact.mobilePhone || '—')}</td>
                    <td>${escapeHtml(contact.landlinePhone || '—')}</td>
                    <td style="text-align:center">${escapeHtml(contact.internalPhone || '—')}</td>
                    <td>${escapeHtml(contact.position || '—')}</td>
                    <td>${escapeHtml(contact.department || '—')}</td>
                    <td>${escapeHtml(contact.building || '—')}</td>
                    <td style="text-align:center">${escapeHtml(contact.cabinet || '—')}</td>
                </tr>
            `;
        });
        
        html += `
                    </tbody>
                </table>
                <div class="footer">* Сгенерировано автоматически в системе Phonambula</div>
            </body>
            </html>
        `;
        
        pdfDiv.innerHTML = html;
        await new Promise(resolve => setTimeout(resolve, 100));
        
        const canvas = await html2canvas(pdfDiv, { scale: 2, logging: false, backgroundColor: '#ffffff' });
        const imgData = canvas.toDataURL('image/png');
        const { jsPDF } = window.jspdf;
        
        const imgWidth = 280;
        const pageHeight = 297;
        const imgHeight = (canvas.height * imgWidth) / canvas.width;
        
        const doc = new jsPDF({ orientation: imgHeight > pageHeight ? 'portrait' : 'landscape', unit: 'mm', format: 'a4' });
        
        if (imgHeight > pageHeight - 20) {
            let heightLeft = imgHeight, position = 0, page = 1;
            while (heightLeft > 0) {
                if (page > 1) doc.addPage();
                const pageCanvas = document.createElement('canvas');
                pageCanvas.width = canvas.width;
                pageCanvas.height = Math.min(canvas.height - position, (pageHeight - 20) * canvas.width / imgWidth);
                const ctx = pageCanvas.getContext('2d');
                ctx.drawImage(canvas, 0, position, canvas.width, pageCanvas.height, 0, 0, pageCanvas.width, pageCanvas.height);
                doc.addImage(pageCanvas.toDataURL('image/png'), 'PNG', 10, 10, imgWidth, pageCanvas.height * imgWidth / canvas.width);
                position += pageCanvas.height;
                heightLeft -= pageCanvas.height;
                page++;
            }
        } else {
            doc.addImage(imgData, 'PNG', 10, 10, imgWidth, imgHeight);
        }
        
        doc.save(`contacts_${new Date().toISOString().slice(0, 10)}.pdf`);
        pdfDiv.innerHTML = '';
        exportBtn.textContent = originalText;
        exportBtn.disabled = false;
        showNotification(`✅ Экспорт завершен! Сохранено ${contacts.length} контактов`, 'success');
        
    } catch (error) {
        console.error('Ошибка при экспорте PDF:', error);
        showNotification('Ошибка при экспорте PDF: ' + error.message, 'error');
        const exportBtn = document.getElementById('exportPdfBtn');
        if (exportBtn) {
            exportBtn.textContent = '📄 Экспорт в PDF';
            exportBtn.disabled = false;
        }
    }
}

// Функция переключения категории
function toggleCategory(element) {
    element.classList.toggle('expanded');
    const subList = element.nextElementSibling;
    if (subList) {
        subList.style.display = element.classList.contains('expanded') ? 'block' : 'none';
    }
}

// Функция редактирования поля
function makeEditable(div, field) {
    if (!isAdmin()) return;
    
    div.removeAttribute('onclick');

    const originalValue = div.innerText === '—' ? '' : div.innerText.trim();

    const input = document.createElement('input');
    input.type = 'text';
    input.value = originalValue;
    input.className = 'edit-input';

    div.replaceWith(input);
    input.focus();

    const saveEdit = () => {
        const newDiv = document.createElement('div');
        newDiv.className = 'detail-value clickable';
        newDiv.innerText = input.value.trim() || '—';

        newDiv.dataset.field = field;
        newDiv.dataset.original = input.value.trim();

        newDiv.setAttribute('onclick', `makeEditable(this,'${field}')`);

        input.replaceWith(newDiv);
        showSaveBtn();
    };

    input.addEventListener('blur', saveEdit);
    input.addEventListener('keypress', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            saveEdit();
        }
    });
}

// Функция редактирования справочника
async function editReference(type, currentId, currentName) {
    if (!isAdmin()) {
        showNotification('У вас нет прав для редактирования', 'error');
        return;
    }

    let modal = document.getElementById('referenceModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'referenceModal';
        modal.className = 'modal';
        document.body.appendChild(modal);
    }

    modal.innerHTML = `<div class="modal-content"><h3>Загрузка...</h3></div>`;
    modal.style.display = 'flex';

    try {
        let endpoint = '';
        let title = '';

        if (type === 'position') {
            endpoint = '/api/posts';
            title = 'Выберите должность';
        } else if (type === 'department') {
            endpoint = '/api/divisions';
            title = 'Выберите отдел';
        } else if (type === 'building') {
            endpoint = '/api/buildings';
            title = 'Выберите корпус';
        }

        const rawItems = await safeApiCall(() => api.get(endpoint), 'Не удалось загрузить данные');

        const items = rawItems.map(item => {
            let name = '';
            if (type === 'position') name = item.namePost;
            if (type === 'department') name = item.nameDivision;
            if (type === 'building') name = item.nameBuilding || item.address;
            return { id: item.id, name: name || '—' };
        }).sort((a, b) => a.name.localeCompare(b.name));

        modal.innerHTML = `
            <div class="modal-content">
                <h3>${escapeHtml(title)}</h3>
                <input type="text" id="referenceSearch" class="modal-search" placeholder="Поиск...">
                <select id="referenceSelect" class="modal-select" size="8">
                    ${items.map(it => `
                        <option value="${it.id}" ${it.id == currentId ? 'selected' : ''}>
                            ${escapeHtml(it.name)}
                        </option>
                    `).join('')}
                </select>
                <div class="modal-buttons">
                    <button class="modal-save" onclick="saveReference('${type}')">Сохранить</button>
                    <button class="modal-cancel" onclick="closeReferenceModal()">Отмена</button>
                </div>
            </div>
        `;

        const searchInput = document.getElementById('referenceSearch');
        const selectEl = document.getElementById('referenceSelect');

        searchInput.addEventListener('input', () => {
            const term = searchInput.value.toLowerCase().trim();
            Array.from(selectEl.options).forEach(opt => {
                opt.style.display = opt.textContent.toLowerCase().includes(term) ? '' : 'none';
            });
        });

    } catch (err) {
        modal.innerHTML = `
            <div class="modal-content">
                <h3>Ошибка</h3>
                <button onclick="closeReferenceModal()">Закрыть</button>
            </div>
        `;
    }
}

// Функция сохранения выбранного справочника
function saveReference(type) {
    const select = document.getElementById('referenceSelect');
    if (!select.value) return;

    const id = select.value;
    const name = select.options[select.selectedIndex].text;

    const div = document.querySelector(`[data-field="${type}"]`);
    div.textContent = name;
    div.dataset.id = id;

    showSaveBtn();
    closeReferenceModal();
    showNotification('Изменение сохранено, нажмите "Сохранить изменения"', 'success');
}

// Функция закрытия модального окна справочника
function closeReferenceModal() {
    const modal = document.getElementById('referenceModal');
    if (modal) {
        modal.style.display = 'none';
        modal.innerHTML = '';
    }
}

// Функция показа кнопки сохранения
function showSaveBtn() {
    const btn = document.getElementById('saveBtn');
    if (btn) btn.style.display = 'inline-block';
}

// Функция сохранения контакта
async function saveContact() {
    if (!isAdmin()) {
        showNotification('У вас нет прав для редактирования контактов', 'error');
        return;
    }
    if (!editedContactId) return;

    const container = document.getElementById('contactDetails');

    const getText = field => {
        const el = container.querySelector(`[data-field="${field}"]`);
        return el ? (el.textContent.trim() === '—' ? '' : el.textContent.trim()) : '';
    };

    const getId = field => {
        const el = container.querySelector(`[data-field="${field}"]`);
        return el?.dataset?.id ? Number(el.dataset.id) : null;
    };

    const data = {};

    const simpleFields = {
        'mobilePhone': 'mobilePhoneNumber',
        'landlinePhone': 'landlinePhoneNumber',
        'internalPhone': 'internalPhoneNumber',
        'cabinet': 'cabinetNumber'
    };

    Object.entries(simpleFields).forEach(([uiKey, backendKey]) => {
        const val = getText(uiKey);
        if (val !== (contactsData[editedContactId][backendKey] || '')) {
            data[backendKey] = val;
        }
    });

    if (getId('position') !== null) data.postId = getId('position');
    if (getId('department') !== null) data.departmentId = getId('department');
    if (getId('building') !== null) data.buildingId = getId('building');

    if (Object.keys(data).length === 0) {
        showNotification('Нет изменений для сохранения', 'info');
        return;
    }

    try {
        const response = await safeApiCall(
            () => api.put(`/api/subscribers/${editedContactId}`, data),
            'Не удалось сохранить изменения'
        );
        
        Object.assign(contactsData[editedContactId], response);
        showContactDetails(editedContactId);
        document.getElementById('saveBtn').style.display = 'none';
        showNotification('Изменения успешно сохранены', 'success');
    } catch (err) {
        console.error(err);
    }
}

// Открыть модальное окно аудита
async function openAuditModal() {
    if (!isAdmin()) {
        showNotification('Доступ запрещен. Только для администраторов', 'error');
        return;
    }
    
    const modal = document.getElementById('auditModal');
    if (modal) {
        modal.style.display = 'flex';
        currentAuditPage = 1;
        await loadAuditLog();
    }
}

// Закрыть модальное окно аудита
function closeAuditModal() {
    const modal = document.getElementById('auditModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Загрузить журнал аудита
async function loadAuditLog() {
    try {
        const searchTerm = document.getElementById('auditSearch')?.value || '';
        const operationFilter = document.getElementById('auditOperationFilter')?.value || '';
        const tableFilter = document.getElementById('auditTableFilter')?.value || '';
        
        // Формируем URL с параметрами
        let url = '/api/audit?page=' + currentAuditPage + '&size=20';
        if (searchTerm) url += '&search=' + encodeURIComponent(searchTerm);
        if (operationFilter) url += '&operation=' + operationFilter;
        if (tableFilter) url += '&table=' + tableFilter;
        
        const response = await safeApiCall(() => api.get(url), 'Не удалось загрузить журнал аудита');
        
        auditData = response.content || [];
        auditTotalPages = response.totalPages || 1;
        
        renderAuditTable();
        updateAuditPagination();
        
    } catch (err) {
        console.error('Ошибка загрузки аудита:', err);
        const tbody = document.getElementById('auditTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: #dc3545;">❌ Ошибка загрузки данных</td></tr>';
        }
    }
}

// Обновить журнал аудита
function refreshAuditLog() {
    currentAuditPage = 1;
    loadAuditLog();
}

// Загрузить страницу аудита
async function loadAuditPage(direction) {
    if (direction === 'prev' && currentAuditPage > 1) {
        currentAuditPage--;
        await loadAuditLog();
    } else if (direction === 'next' && currentAuditPage < auditTotalPages) {
        currentAuditPage++;
        await loadAuditLog();
    }
}

// Обновить пагинацию
function updateAuditPagination() {
    const pageInfo = document.getElementById('auditPageInfo');
    const prevBtn = document.getElementById('prevPageBtn');
    const nextBtn = document.getElementById('nextPageBtn');
    
    if (pageInfo) {
        pageInfo.textContent = `Страница ${currentAuditPage} из ${auditTotalPages}`;
    }
    if (prevBtn) {
        prevBtn.disabled = currentAuditPage <= 1;
    }
    if (nextBtn) {
        nextBtn.disabled = currentAuditPage >= auditTotalPages;
    }
}

// Отобразить таблицу аудита
function renderAuditTable() {
    const tbody = document.getElementById('auditTableBody');
    if (!tbody) return;
    
    if (!auditData || auditData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">📭 Нет записей аудита</td></tr>';
        return;
    }
    
    tbody.innerHTML = auditData.map(log => {
        const operationClass = getOperationClass(log.operationType);
        const operationIcon = getOperationIcon(log.operationType);
        const userName = log.user ? `${log.user.surname || ''} ${log.user.name || ''}`.trim() || log.user.login || 'Система' : 'Система';
        const changesHtml = renderChanges(log.itemBeforeChange, log.itemAfterChange, log.operationType);
        
        return `
            <tr>
                <td style="white-space: nowrap;">${formatDateTime(log.changeTime)}</td>
                <td>${escapeHtml(userName)}</td>
                <td class="${operationClass}">${operationIcon} ${log.operationType}</td>
                <td>${escapeHtml(log.tableName)}</td>
                <td>${log.recordId || '—'}</td>
                <td>${changesHtml}</td>
            </tr>
        `;
    }).join('');
}

// Получить класс для операции
function getOperationClass(operation) {
    switch(operation) {
        case 'INSERT': return 'audit-operation-INSERT';
        case 'UPDATE': return 'audit-operation-UPDATE';
        case 'DELETE': return 'audit-operation-DELETE';
        default: return '';
    }
}

// Получить иконку для операции
function getOperationIcon(operation) {
    switch(operation) {
        case 'INSERT': return '➕';
        case 'UPDATE': return '✏️';
        case 'DELETE': return '🗑️';
        default: return '📝';
    }
}

// Отобразить изменения в читаемом виде
function renderChanges(before, after, operation) {
    if (operation === 'INSERT' && after) {
        return `<div class="json-view" onclick="showJsonDetails(this)">➕ Создано: ${formatJsonPreview(after)}</div>`;
    } else if (operation === 'DELETE' && before) {
        return `<div class="json-view" onclick="showJsonDetails(this)">🗑️ Удалено: ${formatJsonPreview(before)}</div>`;
    } else if (operation === 'UPDATE' && before && after) {
        const changes = getChanges(before, after);
        return `<div class="json-view" onclick="showJsonDetails(this)">✏️ Изменено: ${changes}</div>`;
    }
    return '<span style="color: #999;">—</span>';
}

// Форматировать предпросмотр JSON
function formatJsonPreview(jsonStr) {
    if (!jsonStr) return '—';
    try {
        const obj = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr;
        const keys = Object.keys(obj).slice(0, 3);
        const preview = keys.map(k => `${k}: ${obj[k]}`).join(', ');
        return preview + (Object.keys(obj).length > 3 ? '...' : '');
    } catch (e) {
        return String(jsonStr).substring(0, 50);
    }
}

// Получить изменения между двумя объектами
function getChanges(before, after) {
    try {
        const beforeObj = typeof before === 'string' ? JSON.parse(before) : before;
        const afterObj = typeof after === 'string' ? JSON.parse(after) : after;
        
        const changes = [];
        for (const key in afterObj) {
            if (beforeObj[key] !== afterObj[key]) {
                changes.push(`${key}: ${beforeObj[key]} → ${afterObj[key]}`);
            }
        }
        return changes.slice(0, 3).join(', ') + (changes.length > 3 ? '...' : '');
    } catch (e) {
        return 'Изменения';
    }
}

// Показать полный JSON в диалоге
function showJsonDetails(element) {
    const jsonText = element.textContent;
    const jsonMatch = jsonText.match(/(?:Создано|Удалено|Изменено):\s*(.+)/);
    if (jsonMatch && jsonMatch[1]) {
        alert('Подробные данные:\n' + jsonMatch[1]);
    }
}

// Форматировать дату и время
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '—';
    try {
        const date = new Date(dateTimeStr);
        return date.toLocaleString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    } catch (e) {
        return dateTimeStr;
    }
}

// Добавить обработчики для фильтров аудита
function initAuditFilters() {
    const searchInput = document.getElementById('auditSearch');
    const operationFilter = document.getElementById('auditOperationFilter');
    const tableFilter = document.getElementById('auditTableFilter');
    
    if (searchInput) {
        searchInput.addEventListener('input', debounce(() => {
            currentAuditPage = 1;
            loadAuditLog();
        }, 500));
    }
    
    if (operationFilter) {
        operationFilter.addEventListener('change', () => {
            currentAuditPage = 1;
            loadAuditLog();
        });
    }
    
    if (tableFilter) {
        tableFilter.addEventListener('change', () => {
            currentAuditPage = 1;
            loadAuditLog();
        });
    }
}

// Дебаунс функция
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Показать/скрыть кнопку аудита для админа
function toggleAuditButton() {
    const auditBtn = document.getElementById('viewAuditBtn');
    if (auditBtn) {
        auditBtn.style.display = isAdmin() ? 'flex' : 'none';
    }
}

// Экспортируем функции в глобальную область видимости
window.deleteContact = deleteContact;
window.openAddEmployeeModal = openAddEmployeeModal;
window.closeAddEmployeeModal = closeAddEmployeeModal;
window.saveNewEmployee = saveNewEmployee;
window.exportToPDF = exportToPDF;
window.toggleCategory = toggleCategory;
window.selectContact = selectContact;
window.makeEditable = makeEditable;
window.editReference = editReference;
window.saveReference = saveReference;
window.closeReferenceModal = closeReferenceModal;
window.saveContact = saveContact;
// Добавьте в конец файла home.js
window.openAuditModal = openAuditModal;
window.closeAuditModal = closeAuditModal;
window.refreshAuditLog = refreshAuditLog;
window.loadAuditPage = loadAuditPage;
window.showJsonDetails = showJsonDetails;

window.addEventListener('DOMContentLoaded', async () => {
    if (!api.isAuthenticated()) {
        window.location.href = '/login.html';
        return;
    }
    
    try {
        const userInfo = await safeApiCall(() => api.get('/api/users/me'), 'Не удалось получить данные пользователя');
        currentUserRole = userInfo.role;
        localStorage.setItem('userRole', currentUserRole);
        console.log('User role:', currentUserRole);
    } catch (err) {
        currentUserRole = localStorage.getItem('userRole');
        if (!currentUserRole) {
            showNotification('Ошибка загрузки данных пользователя', 'error');
            setTimeout(() => {
                window.location.href = '/login.html';
            }, 1500);
            return;
        }
    }
    
    const searchInput = document.getElementById('searchInput');
    const clearBtn = document.getElementById('clearSearchBtn');
    
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }
    
    if (clearBtn) {
        clearBtn.addEventListener('click', clearSearch);
    }

    // Инициализация аудита
    initAuditFilters();
    
    // Закрытие модального окна по клику вне его
    const modal = document.getElementById('auditModal');
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeAuditModal();
            }
        });
    }
    
    loadMyContacts();
});