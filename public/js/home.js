let contactsData = {};
let myId = api.myId;
let userRole = api.role;
let editedContactId = null;

// ==================== ЗАГРУЗКА КОНТАКТОВ ====================
async function loadMyContacts() {
    try {
        const data = await api.get('/api/subscribers/my');
        contactsData = {};
        data.forEach(contact => contactsData[contact.id] = contact);
        rebuildContactTree();

        const firstKey = Object.keys(contactsData)[0];
        if (firstKey) showContactDetails(firstKey);
    } catch (err) {
        console.error(err);
        alert('Не удалось загрузить контакты');
    }
}

// ==================== ДЕРЕВО КОНТАКТОВ ====================
function rebuildContactTree() {
    const treeContainer = document.querySelector('.contact-tree');
    treeContainer.innerHTML = '';

    if (!contactsData || Object.keys(contactsData).length === 0) {
        treeContainer.innerHTML = '<li><p>Нет контактов</p></li>';
        return;
    }

    // Находим текущего пользователя в списке контактов
    const currentUserContact = Object.values(contactsData).find(c => c.userId === myId);
    const otherContacts = Object.values(contactsData).filter(c => c.userId !== myId);

    // Блок "Вы" - показываем только контакт текущего пользователя
    if (currentUserContact) {
        treeContainer.innerHTML += `
            <li>
                <div class="category expanded" onclick="toggleCategory(this)">Вы</div>
                <ul class="sub-list">
                    <li class="contact-item active" onclick="selectContact(this, '${currentUserContact.id}')">
                        ${currentUserContact.name}
                        ${userRole === 'Администратор' ? `<button class="delete-btn" onclick="deleteContact(event,'${currentUserContact.id}')">Удалить</button>` : ''}
                    </li>
                </ul>
            </li>
        `;
    }

    // Группировка остальных контактов по первой букве
    if (otherContacts.length > 0) {
        const grouped = {};
        otherContacts.forEach(contact => {
            const firstLetter = contact.name.charAt(0).toUpperCase();
            if (!grouped[firstLetter]) grouped[firstLetter] = [];
            grouped[firstLetter].push(contact);
        });

        Object.keys(grouped).sort().forEach(letter => {
            let html = `
                <li>
                    <div class="category expanded" onclick="toggleCategory(this)">${letter}</div>
                    <ul class="sub-list">
            `;
            grouped[letter].forEach(contact => {
                html += `<li class="contact-item" onclick="selectContact(this, '${contact.id}')">
                            ${contact.name}
                            ${userRole === 'Администратор' ? `<button class="delete-btn" onclick="deleteContact(event,'${contact.id}')">Удалить</button>` : ''}
                         </li>`;
            });
            html += `</ul></li>`;
            treeContainer.innerHTML += html;
        });
    }
}

function toggleCategory(element) {
    element.classList.toggle('expanded');
    const subList = element.nextElementSibling;
    if (subList && subList.classList.contains('sub-list')) {
        subList.style.display = element.classList.contains('expanded') ? 'block' : 'none';
    }
}

function selectContact(element, contactId) {
    document.querySelectorAll('.contact-item').forEach(item => item.classList.remove('active'));
    element.classList.add('active');
    showContactDetails(contactId);
}

// ==================== ОТОБРАЖЕНИЕ КОНТАКТА ====================
function showContactDetails(contactId) {
    editedContactId = contactId;
    const c = contactsData[contactId];
    const editable = userRole === 'Администратор' || c.userId === myId;

    const detailsDiv = document.getElementById('contactDetails');
    detailsDiv.innerHTML = `
        <h2>${c.name}</h2>
        <div class="detail-section">
            <h4>Контактные данные</h4>
            <div class="detail-item">
                <div class="detail-label">Мобильный</div>
                <div class="detail-value ${editable ? "clickable" : ""}" data-field="mobilePhoneNumber" data-original="${c.mobilePhone || ''}" onclick="makeEditable(this, 'mobilePhoneNumber')">${c.mobilePhone || '—'}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Городской</div>
                <div class="detail-value ${editable ? "clickable" : ""}" data-field="landlinePhoneNumber" data-original="${c.landlinePhone || ''}" onclick="makeEditable(this, 'landlinePhoneNumber')">${c.landlinePhone || '—'}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Внутренний</div>
                <div class="detail-value ${editable ? "clickable" : ""}" data-field="internalPhoneNumber" data-original="${c.internalPhone || ''}" onclick="makeEditable(this, 'internalPhoneNumber')">${c.internalPhone || '—'}</div>
            </div>
        </div>
        <div class="detail-section">
            <h4>Информация</h4>
            <div class="detail-item">
                <div class="detail-label">Должность</div>
                <div class="detail-value" data-field="position" data-id="${c.positionId || ''}">${c.position || '—'}</div>
                ${editable ? `<button class="edit-btn" onclick="editReference('position', ${c.positionId || 'null'}, '${c.position || ''}')">✎ Изменить</button>` : ''}
            </div>
            <div class="detail-item">
                <div class="detail-label">Отдел</div>
                <div class="detail-value" data-field="department" data-id="${c.departmentId || ''}">${c.department || '—'}</div>
                ${editable ? `<button class="edit-btn" onclick="editReference('department', ${c.departmentId || 'null'}, '${c.department || ''}')">✎ Изменить</button>` : ''}
            </div>
            <div class="detail-item">
                <div class="detail-label">Корпус</div>
                <div class="detail-value" data-field="building" data-id="${c.buildingId || ''}">${c.building || '—'}</div>
                ${editable ? `<button class="edit-btn" onclick="editReference('building', ${c.buildingId || 'null'}, '${c.building || ''}')">✎ Изменить</button>` : ''}
            </div>
            <div class="detail-item">
                <div class="detail-label">Кабинет</div>
                <div class="detail-value ${editable ? "clickable" : ""}" data-field="cabinetNumber" data-original="${c.cabinet || ''}" onclick="makeEditable(this, 'cabinetNumber')">${c.cabinet || '—'}</div>
            </div>
        </div>
        <div class="save-button-container">
            <button id="saveBtn" style="display:none;" onclick="saveContact()">💾 Сохранить изменения</button>
        </div>
    `;
}

// ==================== INLINE EDIT ДЛЯ ТЕЛЕФОНОВ И КАБИНЕТА ====================
function makeEditable(div, field) {
    // Убираем обработчик, чтобы не создавать множественные поля при повторном клике
    div.removeAttribute('onclick');
    
    const originalValue = div.innerText === '—' ? '' : div.innerText;
    const input = document.createElement('input');
    input.type = 'text';
    input.value = originalValue;
    input.className = 'edit-input';
    input.placeholder = 'Введите значение';
    
    div.replaceWith(input);
    input.focus();

    const saveEdit = () => {
        const newDiv = document.createElement('div');
        newDiv.className = 'detail-value clickable';
        newDiv.innerText = input.value || '—';
        newDiv.dataset.field = field;
        newDiv.dataset.original = input.value;
        newDiv.setAttribute('onclick', `makeEditable(this, '${field}')`);
        input.replaceWith(newDiv);
        showSaveBtn();
    };

    input.addEventListener('blur', saveEdit);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            saveEdit();
        }
    });
}

// ==================== РЕДАКТИРОВАНИЕ СПРАВОЧНИКОВ ====================
async function editReference(type, currentId, currentName) {
    // Создаем или показываем модальное окно
    let modal = document.getElementById('referenceModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'referenceModal';
        modal.className = 'modal';
        document.body.appendChild(modal);
    }

    // Показываем загрузку
    modal.innerHTML = `
        <div class="modal-content">
            <h3>Загрузка...</h3>
        </div>
    `;
    modal.style.display = 'flex';

    try {
        // Загружаем список в зависимости от типа
        let items = [];
        let endpoint = '';
        let title = '';
        
        switch(type) {
            case 'position':
                endpoint = '/api/posts';
                title = 'Выберите должность';
                break;
            case 'department':
                endpoint = '/api/divisions';
                title = 'Выберите отдел';
                break;
            case 'building':
                endpoint = '/api/buildings';
                title = 'Выберите корпус';
                break;
        }

        // Пробуем загрузить с сервера
        try {
            items = await api.get(endpoint);
        } catch (err) {
            console.warn(`Не удалось загрузить ${type}, используем заглушку:`, err);
            // Заглушка для демонстрации
            items = [
                { id: 1, name: currentName || 'Значение 1' },
                { id: 2, name: 'Значение 2' },
                { id: 3, name: 'Значение 3' }
            ];
        }

        // Поле для поиска
        modal.innerHTML = `
            <div class="modal-content">
                <h3>${title}</h3>
                <input type="text" id="referenceSearch" class="modal-search" placeholder="Поиск...">
                <select id="referenceSelect" class="modal-select" size="5">
                    ${items.map(item => 
                        `<option value="${item.id}" ${item.id == currentId ? 'selected' : ''}>${item.name}</option>`
                    ).join('')}
                </select>
                <div class="modal-buttons">
                    <button class="modal-save" onclick="saveReference('${type}')">Сохранить</button>
                    <button class="modal-cancel" onclick="closeReferenceModal()">Отмена</button>
                </div>
            </div>
        `;

        // Добавляем поиск
        const searchInput = document.getElementById('referenceSearch');
        const select = document.getElementById('referenceSelect');
        
        if (searchInput) {
            searchInput.addEventListener('input', () => {
                const searchTerm = searchInput.value.toLowerCase();
                Array.from(select.options).forEach(option => {
                    const text = option.text.toLowerCase();
                    option.style.display = text.includes(searchTerm) ? '' : 'none';
                });
            });
        }

    } catch (err) {
        console.error(err);
        modal.innerHTML = `
            <div class="modal-content">
                <h3>Ошибка</h3>
                <p>Не удалось загрузить данные</p>
                <div class="modal-buttons">
                    <button class="modal-cancel" onclick="closeReferenceModal()">Закрыть</button>
                </div>
            </div>
        `;
    }
}

function saveReference(type) {
    const select = document.getElementById('referenceSelect');
    if (!select) return;
    
    const selectedId = select.value;
    const selectedOption = select.options[select.selectedIndex];
    const selectedName = selectedOption ? selectedOption.text : '';
    
    // Находим соответствующий элемент в деталях контакта
    const detailDiv = document.querySelector(`.contact-details [data-field="${type}"]`);
    if (detailDiv) {
        detailDiv.innerText = selectedName;
        detailDiv.dataset.id = selectedId;
    }
    
    closeReferenceModal();
    showSaveBtn();
}

function closeReferenceModal() {
    const modal = document.getElementById('referenceModal');
    if (modal) {
        modal.style.display = 'none';
        modal.innerHTML = '';
    }
}

// Показываем кнопку Сохранить
function showSaveBtn() {
    const saveBtn = document.getElementById('saveBtn');
    if (saveBtn) {
        saveBtn.style.display = 'inline-block';
        saveBtn.classList.add('visible');
    }
}

// ==================== СОХРАНЕНИЕ ====================
async function saveContact() {
    if (!editedContactId) return;

    const div = document.getElementById('contactDetails');
    const contact = contactsData[editedContactId];
    
    // Собираем все измененные поля
    const updated = {};
    
    // Телефоны и кабинет
    const fields = ['mobilePhoneNumber', 'landlinePhoneNumber', 'internalPhoneNumber', 'cabinetNumber'];
    fields.forEach(field => {
        const element = div.querySelector(`[data-field="${field}"]`);
        if (element) {
            const newValue = element.innerText === '—' ? '' : element.innerText;
            const originalValue = contact[field.replace('Number', '')] || '';
            if (newValue !== originalValue) {
                updated[field] = newValue;
            }
        }
    });
    
    // ID справочников
    const refFields = [
        { field: 'position', idField: 'postId', contactField: 'positionId' },
        { field: 'department', idField: 'departmentId', contactField: 'departmentId' },
        { field: 'building', idField: 'buildingId', contactField: 'buildingId' }
    ];
    
    refFields.forEach(ref => {
        const element = div.querySelector(`[data-field="${ref.field}"]`);
        if (element && element.dataset.id) {
            const newId = parseInt(element.dataset.id);
            const originalId = contact[ref.contactField];
            if (newId !== originalId) {
                updated[ref.idField] = newId;
            }
        }
    });

    // Если ничего не изменилось
    if (Object.keys(updated).length === 0) {
        alert('Нет изменений для сохранения');
        return;
    }

    // Показываем индикатор загрузки
    const saveBtn = document.getElementById('saveBtn');
    const originalText = saveBtn.innerText;
    saveBtn.innerText = 'Сохранение...';
    saveBtn.disabled = true;

    try {
        await api.put(`/api/subscribers/${editedContactId}`, updated);
        alert('✅ Изменения сохранены');
        await loadMyContacts(); // Перезагружаем список
        saveBtn.style.display = 'none';
    } catch (err) {
        console.error('Ошибка сохранения:', err);
        alert('❌ Ошибка при сохранении: ' + (err.message || 'Неизвестная ошибка'));
    } finally {
        saveBtn.innerText = originalText;
        saveBtn.disabled = false;
    }
}

// ==================== УДАЛЕНИЕ ====================
async function deleteContact(event, id) {
    event.stopPropagation();
    if (!confirm('🗑 Удалить контакт? Это действие нельзя отменить.')) return;
    
    try {
        await api.delete(`/api/subscribers/${id}`);
        alert('✅ Контакт удалён');
        
        if (id == editedContactId) {
            editedContactId = null;
        }
        await loadMyContacts();
    } catch (err) {
        console.error(err);
        alert('Ошибка при удалении: ' + (err.message || 'Неизвестная ошибка'));
    }
}

// ==================== ИНИЦИАЛИЗАЦИЯ ====================
window.addEventListener('DOMContentLoaded', () => {
    if (!api.token) {
        window.location.href = '/login.html';
    } else {
        loadMyContacts();
    }
});