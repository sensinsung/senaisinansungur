function followUser(userId) {
    // Kullanıcının kendisini takip etmesini engelle
    const currentUserId = document.querySelector('meta[name="current-user-id"]')?.content;
    if (currentUserId && parseInt(currentUserId) === parseInt(userId)) {
        alert('Kendinizi takip edemezsiniz.');
        return;
    }

    fetch(`/api/follow/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            throw new Error('İşlem başarısız oldu');
        }
    })
    .catch(error => {
        console.error('Hata:', error);
        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
    });
}

function unfollowUser(userId) {
    fetch(`/api/follow/${userId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        }
    });
}

function showFollowers(userId) {
    fetch(`/api/follow/followers/${userId}`, {
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401) {
                throw new Error('Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.');
            }
            throw new Error('Takipçiler getirilirken bir hata oluştu');
        }
        return response.json();
    })
    .then(followers => {
        console.log('Takipçiler API Yanıtı:', followers);
        if (!Array.isArray(followers)) {
            throw new Error('Geçersiz veri formatı');
        }
        
        const followersList = document.getElementById('followersList');
        followersList.innerHTML = '';
        const currentUserId = document.querySelector('meta[name="current-user-id"]')?.content;
        
        followers.forEach(follower => {
            console.log('Takipçi Detayları:', follower);
            const followerItem = document.createElement('div');
            followerItem.className = 'list-group-item d-flex align-items-center justify-content-between';
            
            const userLink = document.createElement('a');
            userLink.href = `/profil/${follower.userid}`;
            userLink.className = 'text-decoration-none text-dark d-flex align-items-center flex-grow-1';
            userLink.innerHTML = `
                <img src="${follower.profilePicture ? `/uploads/profile-photos/${follower.userid}` : '/img/default-profile.svg'}" 
                     class="rounded-circle me-3" style="width: 40px; height: 40px; object-fit: cover;">
                <div>
                    <h6 class="mb-0">${follower.firstName} ${follower.lastName}</h6>
                    <small class="text-muted">@${follower.username}</small>
                </div>
            `;
            
            followerItem.appendChild(userLink);
            
            // Kendi kartımızda hiçbir buton gösterme
            if (currentUserId && parseInt(currentUserId) === parseInt(follower.userid)) {
                // Hiçbir buton ekleme
            }
            // Kendi profilimizde takipçi modalında takipten çıkar butonu göster
            else if (currentUserId && parseInt(currentUserId) === parseInt(userId)) {
                const actionButton = document.createElement('button');
                actionButton.className = 'btn btn-outline-danger btn-sm';
                actionButton.textContent = 'Takipten Çıkar';
                actionButton.onclick = (e) => {
                    e.preventDefault();
                    fetch(`/api/follow/remove-follower/${follower.userid}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                    .then(response => {
                        if (response.ok) {
                            location.reload();
                        } else {
                            throw new Error('İşlem başarısız oldu');
                        }
                    })
                    .catch(error => {
                        console.error('Hata:', error);
                        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
                    });
                };
                followerItem.appendChild(actionButton);
            }
            // Diğer kullanıcılar için normal takip/takibi bırak butonu göster
            else {
                const actionButton = document.createElement('button');
                if (follower.isFollowing) {
                    actionButton.className = 'btn btn-outline-danger btn-sm';
                    actionButton.textContent = 'Takibi Bırak';
                    actionButton.onclick = (e) => {
                        e.preventDefault();
                        unfollowUser(follower.userid);
                    };
                } else {
                    actionButton.className = 'btn btn-primary btn-sm';
                    actionButton.textContent = 'Takip Et';
                    actionButton.onclick = (e) => {
                        e.preventDefault();
                        followUser(follower.userid);
                    };
                }
                followerItem.appendChild(actionButton);
            }
            
            followersList.appendChild(followerItem);
        });
        
        new bootstrap.Modal(document.getElementById('followersModal')).show();
    })
    .catch(error => {
        console.error('Hata:', error);
        if (error.message.includes('Oturum süreniz dolmuş')) {
            alert('Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.');
            window.location.href = '/giris';
        } else {
            alert('Takipçiler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
        }
    });
}

function showFollowing(userId) {
    fetch(`/api/follow/following/${userId}`, {
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401) {
                throw new Error('Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.');
            }
            throw new Error('Takip edilenler getirilirken bir hata oluştu');
        }
        return response.json();
    })
    .then(following => {
        console.log('Takip Edilenler API Yanıtı:', following);
        if (!Array.isArray(following)) {
            throw new Error('Geçersiz veri formatı');
        }
        
        const followingList = document.getElementById('followingList');
        followingList.innerHTML = '';
        const currentUserId = document.querySelector('meta[name="current-user-id"]')?.content;
        
        following.forEach(followed => {
            console.log('Takip Edilen Detayları:', followed);
            const followingItem = document.createElement('div');
            followingItem.className = 'list-group-item d-flex align-items-center justify-content-between';
            
            const userLink = document.createElement('a');
            userLink.href = `/profil/${followed.userid}`;
            userLink.className = 'text-decoration-none text-dark d-flex align-items-center flex-grow-1';
            userLink.innerHTML = `
                <img src="${followed.profilePicture ? `/uploads/profile-photos/${followed.userid}` : '/img/default-profile.svg'}" 
                     class="rounded-circle me-3" style="width: 40px; height: 40px; object-fit: cover;">
                <div>
                    <h6 class="mb-0">${followed.firstName} ${followed.lastName}</h6>
                    <small class="text-muted">@${followed.username}</small>
                </div>
            `;
            
            followingItem.appendChild(userLink);
            
            // Kendi kartımızda hiçbir buton gösterme
            if (currentUserId && parseInt(currentUserId) === parseInt(followed.userid)) {
                // Hiçbir buton ekleme
            }
            // Başkasının profilinde ve beni takip ediyorsa "Takipten Çıkar" butonu göster
            else if (currentUserId && parseInt(currentUserId) === parseInt(followed.userid) && 
                window.location.pathname !== '/profil') {
                const unfollowButton = document.createElement('button');
                unfollowButton.className = 'btn btn-outline-danger btn-sm';
                unfollowButton.textContent = 'Takipten Çıkar';
                unfollowButton.onclick = (e) => {
                    e.preventDefault();
                    fetch(`/api/follow/remove-follower/${userId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                    .then(response => {
                        if (response.ok) {
                            location.reload();
                        } else {
                            throw new Error('İşlem başarısız oldu');
                        }
                    })
                    .catch(error => {
                        console.error('Hata:', error);
                        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
                    });
                };
                followingItem.appendChild(unfollowButton);
            }
            // Diğer kullanıcılar için normal takip/takibi bırak butonu göster
            else {
                const actionButton = document.createElement('button');
                if (followed.isFollowing) {
                    actionButton.className = 'btn btn-outline-danger btn-sm';
                    actionButton.textContent = 'Takibi Bırak';
                    actionButton.onclick = (e) => {
                        e.preventDefault();
                        unfollowUser(followed.userid);
                    };
                } else {
                    actionButton.className = 'btn btn-primary btn-sm';
                    actionButton.textContent = 'Takip Et';
                    actionButton.onclick = (e) => {
                        e.preventDefault();
                        followUser(followed.userid);
                    };
                }
                followingItem.appendChild(actionButton);
            }
            
            followingList.appendChild(followingItem);
        });
        
        new bootstrap.Modal(document.getElementById('followingModal')).show();
    })
    .catch(error => {
        console.error('Hata:', error);
        if (error.message.includes('Oturum süreniz dolmuş')) {
            alert('Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.');
            window.location.href = '/giris';
        } else {
            alert('Takip edilenler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
        }
    });
}

// Modal yönetimi için özel fonksiyon
function showFollowingModal() {
    const modal = document.getElementById('followingModal');
    const modalInstance = new bootstrap.Modal(modal, {
        keyboard: true,
        focus: true,
        backdrop: 'static'
    });
    
    // Modal açılmadan önce
    modal.addEventListener('show.bs.modal', function () {
        modal.removeAttribute('aria-hidden');
        modal.setAttribute('aria-modal', 'true');
    });
    
    // Modal kapanmadan önce
    modal.addEventListener('hide.bs.modal', function () {
        modal.setAttribute('aria-hidden', 'true');
        modal.removeAttribute('aria-modal');
    });
    
    modalInstance.show();
}

// Takip edilenleri yükleme fonksiyonu
function loadFollowing() {
    fetch('/api/follow/following')
        .then(response => response.json())
        .then(data => {
            const followingList = document.getElementById('followingList');
            followingList.innerHTML = '';
            
            data.forEach(following => {
                const followingItem = document.createElement('div');
                followingItem.className = 'list-group-item d-flex justify-content-between align-items-center';
                
                const userInfo = document.createElement('div');
                userInfo.innerHTML = `
                    <a href="/profil/${following.userid}" class="text-decoration-none">
                        <strong>${following.username}</strong>
                    </a>
                `;
                
                followingItem.appendChild(userInfo);
                
                const unfollowButton = document.createElement('button');
                unfollowButton.className = 'btn btn-outline-danger btn-sm';
                unfollowButton.textContent = 'Takipten Çıkar';
                unfollowButton.onclick = (e) => {
                    e.preventDefault();
                    fetch(`/api/follow/unfollow/${following.userid}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                    .then(response => {
                        if (response.ok) {
                            location.reload();
                        } else {
                            throw new Error('İşlem başarısız oldu');
                        }
                    })
                    .catch(error => {
                        console.error('Hata:', error);
                        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
                    });
                };
                
                followingItem.appendChild(unfollowButton);
                followingList.appendChild(followingItem);
            });
            
            showFollowingModal();
        })
        .catch(error => {
            console.error('Hata:', error);
            alert('Takip edilenler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
        });
}

function sendFollowRequest(userId) {
    fetch(`/api/follow/request/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            alert('Takip isteği gönderildi.');
            location.reload();
        } else {
            throw new Error('İşlem başarısız oldu');
        }
    })
    .catch(error => {
        console.error('Hata:', error);
        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
    });
}

function acceptFollowRequest(requestId) {
    fetch(`/api/follow/request/${requestId}/accept`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            throw new Error('İşlem başarısız oldu');
        }
    })
    .catch(error => {
        console.error('Hata:', error);
        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
    });
}

function rejectFollowRequest(requestId) {
    fetch(`/api/follow/request/${requestId}/reject`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            throw new Error('İşlem başarısız oldu');
        }
    })
    .catch(error => {
        console.error('Hata:', error);
        alert('İşlem sırasında bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
    });
}

// Bildirimleri okundu olarak işaretle
function markNotificationsAsRead() {
    fetch('/api/notifications/mark-as-read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Bildirimler okundu olarak işaretlenemedi');
        }
        // Bildirim sayacını güncelle
        updateNotificationBadge();
    })
    .catch(error => {
        console.error('Hata:', error);
    });
}

// Tüm bildirimleri sil
function deleteAllNotifications() {
    if (!confirm('Tüm bildirimlerinizi silmek istediğinizden emin misiniz?')) {
        return;
    }

    fetch('/api/notifications/delete-all', {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Bildirimler silinemedi');
        }
        
        // Bildirim sayacını güncelle
        const badge = document.querySelector('#newNotificationsDropdown .badge');
        if (badge) {
            badge.textContent = '0';
            badge.classList.add('d-none');
        }

        // Bildirim listesini temizle
        const notificationsContainer = document.querySelector('.notifications-container');
        if (notificationsContainer) {
            notificationsContainer.innerHTML = `
                <div class="text-center text-muted py-3">
                    <i class="bi bi-envelope-open fs-1"></i>
                    <p class="mt-2 mb-0">Yeni bildirim bulunmuyor</p>
                </div>
            `;
        }

        // Dropdown'ı kapat
        const dropdownButton = document.getElementById('newNotificationsDropdown');
        if (dropdownButton) {
            const dropdown = new bootstrap.Dropdown(dropdownButton);
            dropdown.hide();
        }
    })
    .catch(error => {
        console.error('Hata:', error);
        alert('Bildirimler silinirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
    });
}

// Bildirim sayacını güncelle
function updateNotificationBadge() {
    fetch('/api/notifications/unread/count')
        .then(response => response.json())
        .then(count => {
            const badge = document.querySelector('#newNotificationsDropdown .badge');
            if (badge) {
                badge.textContent = count;
                if (count === 0) {
                    badge.classList.add('d-none');
                } else {
                    badge.classList.remove('d-none');
                }
            }
        })
        .catch(error => {
            console.error('Bildirim sayacı güncellenirken hata:', error);
        });
}

// Bildirim paneli açıldığında bildirimleri okundu olarak işaretle
document.addEventListener('DOMContentLoaded', function() {
    const notificationDropdown = document.getElementById('newNotificationsDropdown');
    if (notificationDropdown) {
        notificationDropdown.addEventListener('show.bs.dropdown', function () {
            markNotificationsAsRead();
        });
    }
}); 