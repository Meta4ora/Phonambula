let contactsData = {};

async function loadMyContacts() {
    try {
        const response = await api.get('/api/subscribers/my');
        
        const data = Array.isArray(response) ? response : (response.data || []);

        console.log('Получено контактов:', data.length);

        contactsData = {};

        data.forEach(contact => {
            const key = contact.key;
            if (!key) return;

            contactsData[key] = {
                name: contact.name || 'Без имени',
                phone: contact.mobilePhone || contact.landlinePhone || contact.internalPhone || '',
                cityPhone: contact.landlinePhone || '',
                email: '',
                position: contact.position || '',
                department: contact.department || '',
                building: contact.building || '',
                cabinet: contact.cabinet || ''
            };
        });

        rebuildContactTree();
        
        const firstKey = Object.keys(contactsData)[0];
        if (firstKey) {
            showContactDetails(firstKey);
        }
    } catch (error) {
        console.error('Ошибка загрузки контактов:', error);
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

    const firstKey = Object.keys(contactsData)[0];
    const firstContact = contactsData[firstKey];

    treeContainer.innerHTML += `
        <li>
            <div class="category expanded" onclick="toggleCategory(this)">Вы</div>
            <ul class="sub-list">
                <li class="contact-item active" onclick="selectContact(this, '${firstKey}')">${firstContact.name}</li>
            </ul>
        </li>
    `;

    const grouped = {};
    Object.entries(contactsData).slice(1).forEach(([key, contact]) => {
        const firstLetter = contact.name.charAt(0).toUpperCase();
        if (!grouped[firstLetter]) grouped[firstLetter] = [];
        grouped[firstLetter].push({key, contact});
    });

    Object.keys(grouped).sort().forEach(letter => {
        let html = `
            <li>
                <div class="category expanded" onclick="toggleCategory(this)">${letter}</div>
                <ul class="sub-list">
        `;
        grouped[letter].forEach(item => {
            html += `<li class="contact-item" onclick="selectContact(this, '${item.key}')">${item.contact.name}</li>`;
        });
        html += `</ul></li>`;
        treeContainer.innerHTML += html;
    });
}

function toggleCategory(element) {
    element.classList.toggle('expanded');
}

function selectContact(element, contactId) {
    document.querySelectorAll('.contact-item').forEach(item => item.classList.remove('active'));
    element.classList.add('active');
    showContactDetails(contactId);
}

function showContactDetails(contactId) {
    const contact = contactsData[contactId];
    if (!contact) return;

    const detailsDiv = document.getElementById('contactDetails');
    detailsDiv.innerHTML = `
        <h2>${contact.name}</h2>
        <div class="detail-section">
            <h4>Контактные данные</h4>
            <div class="detail-item"><div class="detail-label">Мобильный</div><div class="detail-value">${contact.phone}</div></div>
            <div class="detail-item"><div class="detail-label">Городской</div><div class="detail-value">${contact.cityPhone}</div></div>
        </div>
        <div class="detail-section">
            <h4>Информация</h4>
            <div class="detail-item"><div class="detail-label">Должность</div><div class="detail-value">${contact.position}</div></div>
            <div class="detail-item"><div class="detail-label">Отдел</div><div class="detail-value">${contact.department}</div></div>
            <div class="detail-item"><div class="detail-label">Корпус</div><div class="detail-value">${contact.building}</div></div>
            <div class="detail-item"><div class="detail-label">Кабинет</div><div class="detail-value">${contact.cabinet}</div></div>
        </div>
    `;
}

window.addEventListener('DOMContentLoaded', () => {
    if (api.token) {
        loadMyContacts();
    } else {
        window.location.href = '/login.html';
    }
});