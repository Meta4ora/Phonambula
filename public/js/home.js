let contactsData = {};
let editedContactId = null;

async function loadMyContacts() {
    try {
        const data = await api.get('/api/subscribers/my');
        contactsData = {};
        data.forEach(contact => {
            contactsData[contact.id] = contact;
        });
        rebuildContactTree();
        const firstKey = Object.keys(contactsData)[0];
        if (firstKey) {
            showContactDetails(firstKey);
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
            html += `
                <li class="contact-item" onclick="selectContact(this,'${contact.id}')">
                    ${contact.name || '—'}
                    <button class="delete-btn" onclick="deleteContact(event,'${contact.id}')">Удалить</button>
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

    detailsDiv.innerHTML = `
    <h2>${c.name || '—'}</h2>

    <div class="detail-section">
        <h4>Контактные данные</h4>

        <div class="detail-item">
            <div class="detail-label">Мобильный</div>
            <div class="detail-value clickable"
                 data-field="mobilePhone"
                 data-original="${c.mobilePhone || ''}"
                 onclick="makeEditable(this, 'mobilePhone')">
                ${c.mobilePhone || '—'}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Городской</div>
            <div class="detail-value clickable"
                 data-field="landlinePhone"
                 data-original="${c.landlinePhone || ''}"
                 onclick="makeEditable(this, 'landlinePhone')">
                ${c.landlinePhone || '—'}
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Внутренний</div>
            <div class="detail-value clickable"
                 data-field="internalPhone"
                 data-original="${c.internalPhone || ''}"
                 onclick="makeEditable(this, 'internalPhone')">
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
                <button class="edit-btn"
                    onclick="editReference('position', ${c.positionId || 'null'}, '${(c.position || '').replace(/'/g,"\\'")}')">
                    Изменить
                </button>
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
                <button class="edit-btn"
                    onclick="editReference('department', ${c.departmentId || 'null'}, '${(c.department || '').replace(/'/g,"\\'")}')">
                    Изменить
                </button>
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
                <button class="edit-btn"
                    onclick="editReference('building', ${c.buildingId || 'null'}, '${(c.building || '').replace(/'/g,"\\'")}')">
                    Изменить
                </button>
            </div>
        </div>

        <div class="detail-item">
            <div class="detail-label">Кабинет</div>
            <div class="detail-value clickable"
                 data-field="cabinet"
                 data-original="${c.cabinet || ''}"
                 onclick="makeEditable(this, 'cabinet')">
                ${c.cabinet || '—'}
            </div>
        </div>
    </div>

    <!-- кнопка сохранения остаётся -->
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

async function deleteContact(event,id) {

    event.stopPropagation();

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

window.addEventListener('DOMContentLoaded', () => {

    if (!api?.token) {
        window.location.href = '/login.html';
    } else {
        loadMyContacts();
    }

});