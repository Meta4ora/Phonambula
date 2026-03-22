let contactsData = {};
let editedContactId = null;
let currentUserRole = null; // Добавляем переменную для роли

// Функция для проверки, является ли пользователь администратором
function isAdmin() {
    console.log('Current user role:', currentUserRole); // Для отладки
    
    // Проверяем различные варианты названия роли
    return currentUserRole === 'Администратор' || 
           currentUserRole === 'ROLE_Администратор';
}

async function loadMyContacts() {
    try {
        const data = await api.get('/api/subscribers/my');
        contactsData = {};
        data.forEach(contact => {
            contactsData[contact.id] = contact;
        });
        
        // Загружаем роль текущего пользователя, если еще не загружена
        if (!currentUserRole) {
            // Получаем роль из localStorage
            currentUserRole = localStorage.getItem('userRole');
            
            // Если роль не сохранена, можно получить из API
            if (!currentUserRole) {
                try {
                    const userInfo = await api.get('/api/users/me');
                    currentUserRole = userInfo.role;
                    localStorage.setItem('userRole', currentUserRole);
                } catch (err) {
                    console.error('Не удалось получить роль пользователя', err);
                }
            }
        }
        
        rebuildContactTree();
        const firstKey = Object.keys(contactsData)[0];
        if (firstKey) {
            showContactDetails(firstKey);
        }
        
        // Показываем или скрываем кнопку добавления сотрудника
        const addEmployeeBtn = document.getElementById('addEmployeeBtn');
        if (addEmployeeBtn) {
            addEmployeeBtn.style.display = isAdmin() ? 'block' : 'none';
        }
        
    } catch (err) {
        console.error("Ошибка загрузки контактов:", err);
        alert('Не удалось загрузить контакты');
    }
}

function rebuildContactTree() {
    const treeContainer = document.querySelector('.contact-tree');
    if (!treeContainer) return;

    treeContainer.innerHTML = '';

    if (Object.keys(contactsData).length === 0) {
        treeContainer.innerHTML = '<li><p>Нет контактов</p></li>';
        return;
    }

    let html = '';
    const contacts = Object.values(contactsData);
    const grouped = {};

    contacts.forEach(contact => {
        const firstLetter = (contact.name || '?')[0].toUpperCase();
        if (!grouped[firstLetter]) grouped[firstLetter] = [];
        grouped[firstLetter].push(contact);
    });

    Object.keys(grouped).sort().forEach(letter => {
        html += `
            <li>
                <div class="category expanded" onclick="toggleCategory(this)">${letter}</div>
                <ul class="sub-list">
        `;

        grouped[letter].forEach(contact => {
            // Кнопка удаления только для администраторов
            const deleteButton = isAdmin() 
                ? `<button class="delete-btn" onclick="deleteContact(event,'${contact.id}')">Удалить</button>`
                : '';
                
            html += `
                <li class="contact-item" onclick="selectContact(this,'${contact.id}')">
                    ${contact.name || '—'}
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
}

function toggleCategory(element) {
    element.classList.toggle('expanded');
    const subList = element.nextElementSibling;
    if (subList) {
        subList.style.display = element.classList.contains('expanded') ? 'block' : 'none';
    }
}

function selectContact(element, contactId) {
    document.querySelectorAll('.contact-item').forEach(i => i.classList.remove('active'));
    element.classList.add('active');
    showContactDetails(contactId);
}

function showContactDetails(contactId) {
    editedContactId = contactId;
    const c = contactsData[contactId];
    if (!c) return;

    const detailsDiv = document.getElementById('contactDetails');
    if (!detailsDiv) return;

    // Определяем, видимы ли кнопки редактирования
    const showEditButtons = isAdmin();
    
    // Формируем кнопки изменения для справочников
    const editButtonHtml = (field, id, name) => showEditButtons 
        ? `<button class="edit-btn" onclick="editReference('${field}', ${id || 'null'}, '${(name || '').replace(/'/g,"\\'")}')">Изменить</button>`
        : '';

    detailsDiv.innerHTML = `
    <h2>${c.name || '—'}</h2>

    <div class="detail-section">
        <h4>Контактные данные</h4>

        <div class="detail-item">
            <div class="detail-label">Мобильный</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="mobilePhone"
                 data-original="${c.mobilePhone || ''}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'mobilePhone')"` : ''}>
                ${c.mobilePhone || '—'}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Городской</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="landlinePhone"
                 data-original="${c.landlinePhone || ''}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'landlinePhone')"` : ''}>
                ${c.landlinePhone || '—'}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Внутренний</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="internalPhone"
                 data-original="${c.internalPhone || ''}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'internalPhone')"` : ''}>
                ${c.internalPhone || '—'}
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
                    ${c.position || '—'}
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
                    ${c.department || '—'}
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
                    ${c.building || '—'}
                </div>
                ${editButtonHtml('building', c.buildingId, c.building)}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Кабинет</div>
            <div class="detail-value ${showEditButtons ? 'clickable' : ''}"
                 data-field="cabinet"
                 data-original="${c.cabinet || ''}"
                 ${showEditButtons ? `onclick="makeEditable(this, 'cabinet')"` : ''}>
                ${c.cabinet || '—'}
            </div>
        </div>
    </div>

    <!-- кнопка сохранения только для администраторов -->
    <div class="save-button-container">
        <button id="saveBtn" style="display:none;" onclick="saveContact()">Сохранить изменения</button>
    </div>
`;
}

function makeEditable(div, field) {
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

async function editReference(type, currentId) {

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
        }

        if (type === 'department') {
            endpoint = '/api/divisions';
            title = 'Выберите отдел';
        }

        if (type === 'building') {
            endpoint = '/api/buildings';
            title = 'Выберите корпус';
        }

        const rawItems = await api.get(endpoint);

        const items = rawItems.map(item => {

            let name = '';

            if (type === 'position') name = item.namePost;
            if (type === 'department') name = item.nameDivision;
            if (type === 'building') name = item.nameBuilding || item.address;

            return {
                id: item.id,
                name: name || '—'
            };

        }).sort((a, b) => a.name.localeCompare(b.name));

        modal.innerHTML = `
            <div class="modal-content">
                <h3>${title}</h3>

                <input type="text" id="referenceSearch" class="modal-search" placeholder="Поиск...">

                <select id="referenceSelect" class="modal-select" size="8">
                    ${items.map(it =>
                        `<option value="${it.id}" ${it.id == currentId ? 'selected' : ''}>
                            ${it.name}
                        </option>`
                    ).join('')}
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

        console.error(err);

        modal.innerHTML = `
            <div class="modal-content">
                <h3>Ошибка</h3>
                <button onclick="closeReferenceModal()">Закрыть</button>
            </div>
        `;
    }
}

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
}

function closeReferenceModal() {

    const modal = document.getElementById('referenceModal');

    modal.style.display = 'none';
    modal.innerHTML = '';
}

function showSaveBtn() {

    const btn = document.getElementById('saveBtn');

    if (btn) {
        btn.style.display = 'inline-block';
    }
}

async function saveContact() {
    if (!isAdmin()) {
        alert('У вас нет прав для редактирования контактов');
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

    // Простые поля
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

    // Справочники (только если изменился ID)
    if (getId('position') !== null) {
        data.postId = getId('position');
    }
    if (getId('department') !== null) {
        data.departmentId = getId('department');
    }
    if (getId('building') !== null) {
        data.buildingId = getId('building');
    }

    if (Object.keys(data).length === 0) {
        alert('Нет изменений');
        return;
    }

    try {
        const response = await api.put(`/api/subscribers/${editedContactId}`, data);
        // Обновляем локальные данные
        Object.assign(contactsData[editedContactId], response);
        showContactDetails(editedContactId);
        document.getElementById('saveBtn').style.display = 'none';
        alert('Сохранено');
    } catch (err) {
        console.error(err);
        alert('Ошибка сохранения: ' + (err.message || '403/400/500'));
    }
}

async function deleteContact(event, id) {
    event.stopPropagation();
    
    // Проверяем права
    if (!isAdmin()) {
        alert('У вас нет прав для удаления контактов');
        return;
    }

    if (!confirm('Удалить контакт?')) return;

    try {
        await api.delete(`/api/subscribers/${id}`);
        alert('Контакт удалён');

        if (String(id) === String(editedContactId)) {
            editedContactId = null;
        }

        await loadMyContacts();
    } catch (err) {
        console.error(err);
        alert('Ошибка удаления');
    }
}

async function openAddEmployeeModal() {
    // Загружаем данные перед открытием
    await loadReferenceDataForModal();
    document.getElementById('addEmployeeModal').style.display = 'flex';
}

function closeAddEmployeeModal() {
    document.getElementById('addEmployeeModal').style.display = 'none';
}

async function loadReferenceDataForModal() {
    try {
        // Загружаем должности
        const positions = await api.get('/api/posts');
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

        // Загружаем отделы
        const divisions = await api.get('/api/divisions');
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

        // Загружаем корпуса
        const buildings = await api.get('/api/buildings');
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

        // Загружаем роли
        const roles = await api.get('/api/roles');
        const roleSelect = document.getElementById('empRole');
        if (roleSelect) {
            roleSelect.innerHTML = '<option value="">Не выбрано</option>';
            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.id;
                // Используем правильное поле из модели Role
                option.textContent = role.nameRole || role.name;
                roleSelect.appendChild(option);
            });
        }

    } catch (err) {
        console.error('Ошибка загрузки справочников:', err);
        alert('Ошибка загрузки данных: ' + err.message);
    }
}


async function saveNewEmployee() {

    if (!isAdmin()) {
        alert('У вас нет прав для добавления сотрудников');
        return;
    }
    // Проверка паролей
    const password = document.getElementById('empPassword').value;
    const confirmPassword = document.getElementById('empConfirmPassword').value;
    
    if (password !== confirmPassword) {
        alert('Пароли не совпадают');
        return;
    }
    
    if (!password || password.length < 4) {
        alert('Пароль должен содержать минимум 4 символа');
        return;
    }
    
    const surname = document.getElementById('empSurname').value.trim();
    const name = document.getElementById('empName').value.trim();
    const patronymic = document.getElementById('empPatronymic').value.trim();
    const login = document.getElementById('empLogin').value.trim();
    
    if (!surname || !name || !login) {
        alert('Пожалуйста, заполните все обязательные поля (Фамилия, Имя, Логин)');
        return;
    }
    
    // Получаем значения из select
    const roleId = document.getElementById('empRole').value;
    const postId = document.getElementById('empPosition').value;
    const divisionId = document.getElementById('empDivision').value;
    const buildingId = document.getElementById('empBuilding').value;
    
    // Собираем данные для отправки
    const data = {
        // Поля для создания пользователя
        surname: surname,
        name: name,
        patronymic: patronymic || "",
        login: login,
        password: password,
        roleId: roleId ? Number(roleId) : null,
        
        // Поля для создания абонента
        postId: postId ? Number(postId) : null,
        divisionId: divisionId ? Number(divisionId) : null,
        buildingId: buildingId ? Number(buildingId) : null,
        
        // Телефоны и кабинет
        mobilePhoneNumber: document.getElementById('empMobilePhone').value.trim() || "",
        landlinePhoneNumber: document.getElementById('empLandlinePhone').value.trim() || "",
        internalPhoneNumber: document.getElementById('empInternalPhone').value.trim() || "",
        cabinetNumber: document.getElementById('empCabinet').value.trim() || ""
    };
    
    try {
        console.log('Отправка данных:', data);
        
        // Отправляем запрос на регистрацию
        const response = await api.post('/auth/register', data);
        
        console.log('Ответ сервера:', response);
        alert('Сотрудник успешно добавлен');
        closeAddEmployeeModal();
        
        // Очистка формы
        document.querySelectorAll('#addEmployeeModal input').forEach(i => i.value = '');
        document.querySelectorAll('#addEmployeeModal select').forEach(s => s.value = '');
        
        // Сохраняем токен, если он вернулся
        if (response.token) {
            api.setToken(response.token);
        }
        
        // Перезагружаем список контактов
        await loadMyContacts();
        
    } catch (err) {
        console.error('Ошибка при добавлении:', err);
        alert('Ошибка добавления: ' + (err.message || 'Сервер не ответил'));
    }
}

window.addEventListener('DOMContentLoaded', async () => {
    if (!api.isAuthenticated()) {
        window.location.href = '/login.html';
        return;
    }
    
    try {
        // Всегда получаем актуальную роль из API
        const userInfo = await api.get('/api/users/me');
        currentUserRole = userInfo.role;
        localStorage.setItem('userRole', currentUserRole);
        console.log('Current user role from API:', currentUserRole);
    } catch (err) {
        console.error('Failed to get user role:', err);
        // Если не удалось получить роль, пробуем из localStorage
        currentUserRole = localStorage.getItem('userRole');
        if (!currentUserRole) {
            alert('Ошибка загрузки данных пользователя');
            window.location.href = '/login.html';
            return;
        }
    }
    
    loadMyContacts();
});