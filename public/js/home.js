const contactsData = {
    self: {
        name: 'Петров Петр Петрович',
        phone: '89812345678',
        cityPhone: '89812345678',
        email: 'gycgdu@hsudh.com',
        position: 'СММ специалист',
        department: 'Отдел АСУП',
        building: '4',
        cabinet: '123'
    },
    favorite: {
        name: 'Рината Абдурахманова',
        phone: '89812345678',
        cityPhone: '89812345678',
        email: 'rinata@company.ru',
        position: 'Главный бухгалтер',
        department: 'Бухгалтерия',
        building: '2',
        cabinet: '45'
    },
    abu: {
        name: 'Абу Калисстский',
        phone: '89812345678',
        cityPhone: '89812345678',
        email: 'abu@company.ru',
        position: 'Специалист',
        department: 'Технический отдел',
        building: '3',
        cabinet: '78'
    },
    abdur: {
        name: 'Абдурахмед Ашотович',
        phone: '89812345678',
        cityPhone: '89812345678',
        email: 'abdu@company.ru',
        position: 'Менеджер',
        department: 'Отдел продаж',
        building: '1',
        cabinet: '12'
    }
};

function toggleCategory(element) {
    element.classList.toggle('expanded');
}

function selectContact(element, contactId) {
    document.querySelectorAll('.contact-item').forEach(item => {
        item.classList.remove('active');
    });
    
    element.classList.add('active');
    showContactDetails(contactId);
}

function showContactDetails(contactId) {
    const contact = contactsData[contactId];
    const detailsDiv = document.getElementById('contactDetails');
    
    detailsDiv.innerHTML = `
        <h2>${contact.name}</h2>
        
        <div class="detail-section">
            <h4>Контактные данные</h4>
            <div class="detail-item">
                <div class="detail-label">Номер телефона</div>
                <div class="detail-value">${contact.phone}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Городской телефон</div>
                <div class="detail-value">${contact.cityPhone}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Адрес электронной почты</div>
                <div class="detail-value">${contact.email}</div>
            </div>
        </div>
        
        <div class="detail-section">
            <h4>Информация</h4>
            <div class="detail-item">
                <div class="detail-label">Должность</div>
                <div class="detail-value small">${contact.position}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Подразделение</div>
                <div class="detail-value small">${contact.department}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Корпус</div>
                <div class="detail-value small">${contact.building}</div>
            </div>
            <div class="detail-item">
                <div class="detail-label">Номер кабинета</div>
                <div class="detail-value small">${contact.cabinet}</div>
            </div>
        </div>
    `;
}

window.addEventListener('DOMContentLoaded', () => {
    showContactDetails('self');
});