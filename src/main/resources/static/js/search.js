document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.querySelector('input[name="query"]');
    const searchResults = document.createElement('div');
    searchResults.className = 'search-results position-absolute bg-white shadow-sm rounded p-2';
    searchResults.style.display = 'none';
    searchResults.style.zIndex = '1000';
    searchResults.style.width = '300px';
    searchResults.style.maxHeight = '400px';
    searchResults.style.overflowY = 'auto';
    searchResults.style.top = '100%';
    searchResults.style.left = '0';
    searchResults.style.marginTop = '5px';
    
    // Arama kutusunun parent elementini relative yap
    searchInput.parentElement.style.position = 'relative';
    searchInput.parentElement.appendChild(searchResults);

    let searchTimeout;

    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        const query = this.value.trim();
        
        if (query.length < 2) {
            searchResults.style.display = 'none';
            return;
        }

        searchTimeout = setTimeout(() => {
            fetch(`/api/users/search?query=${encodeURIComponent(query)}`)
                .then(response => response.json())
                .then(users => {
                    searchResults.innerHTML = '';
                    
                    if (users.length === 0) {
                        searchResults.innerHTML = '<div class="p-2 text-muted">Sonuç bulunamadı</div>';
                    } else {
                        users.forEach(user => {
                            const userCard = document.createElement('div');
                            userCard.className = 'user-card p-2 border-bottom';
                            userCard.style.cursor = 'pointer';
                            
                            const profilePic = user.hasProfilePicture ? 
                                `<img src="/uploads/profile-photos/${user.userid}" class="rounded-circle me-2" style="width: 32px; height: 32px; object-fit: cover;">` :
                                `<i class="bi bi-person-circle me-2"></i>`;
                            
                            userCard.innerHTML = `
                                <div class="d-flex align-items-center">
                                    ${profilePic}
                                    <div>
                                        <div class="fw-bold">${user.firstName} ${user.lastName}</div>
                                        <div class="text-muted small">@${user.username}</div>
                                    </div>
                                </div>
                            `;
                            
                            userCard.addEventListener('click', () => {
                                window.location.href = `/profil/${user.username}`;
                            });
                            
                            searchResults.appendChild(userCard);
                        });
                    }
                    
                    searchResults.style.display = 'block';
                });
        }, 300);
    });

    // Arama sonuçlarını dışarı tıklandığında kapat
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
            searchResults.style.display = 'none';
        }
    });
});

// Form submit işlemini yönet
function handleSearch(event) {
    const searchInput = document.querySelector('input[name="query"]');
    const query = searchInput.value.trim();
    
    if (query.length < 2) {
        event.preventDefault();
        return false;
    }
    
    return true;
} 