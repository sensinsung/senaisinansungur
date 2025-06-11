document.addEventListener('DOMContentLoaded', () => {
    const [f, l] = ['postForm', 'postsList'].map(id => document.getElementById(id));
    
    // Global değişkenler
    window.editSelectedPhotos = [];
    window.editExistingPhotoIds = [];
    
    // Gizlilik ayarları için dropdown işlemleri
    const privacyButton = document.querySelector('#privacyDropdown');
    const privacyText = document.querySelector('.privacy-text');
    const privacyInput = document.querySelector('input[name="privacyType"]');

    // Düzenleme modalı için gizlilik dropdown işlemleri
    const editPrivacyButton = document.querySelector('#editPrivacyDropdown');
    const editPrivacyText = document.querySelector('.edit-privacy-text');
    const editPrivacyInput = document.querySelector('#editPrivacyType');

    // Düzenleme modalı için fotoğraf işlemleri
    const editPhotoInput = document.getElementById('editPhotoInput');
    const editPhotoPreview = document.getElementById('editPhotoPreview');
    const editRemovePhotoBtn = document.getElementById('editRemovePhotoBtn');
    const editExistingPhotos = document.getElementById('editExistingPhotos');

    // Fotoğraf ekleme butonuna tıklama olayı
    document.querySelector('label[for="editPhotoInput"]')?.addEventListener('click', () => {
        editPhotoInput?.click();
    });

    editPhotoInput?.addEventListener('change', (e) => {
        console.log('Fotoğraf seçildi:', e.target.files);
        const files = Array.from(e.target.files);
        window.editSelectedPhotos = [...window.editSelectedPhotos, ...files];
        console.log('Seçilen fotoğraflar:', window.editSelectedPhotos);
        updateEditPhotoPreview();
        if (window.editSelectedPhotos.length > 0) {
            editRemovePhotoBtn?.classList.remove('d-none');
        }
    });

    editRemovePhotoBtn?.addEventListener('click', () => {
        window.editSelectedPhotos = [];
        updateEditPhotoPreview();
        editPhotoInput.value = '';
        editRemovePhotoBtn.classList.add('d-none');
    });

    window.updateEditPhotoPreview = function() {
        if (!editPhotoPreview) {
            console.error('editPhotoPreview elementi bulunamadı');
            return;
        }
        
        console.log('Fotoğraf önizleme güncelleniyor');
        editPhotoPreview.innerHTML = '';
        window.editSelectedPhotos.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const div = document.createElement('div');
                div.className = 'position-relative d-inline-block me-2 mb-2';
                div.innerHTML = `
                    <img src="${e.target.result}" class="img-thumbnail" style="width: 100px; height: 100px; object-fit: cover;">
                    <button type="button" class="btn btn-danger btn-sm position-absolute top-0 end-0 m-1" 
                            onclick="removeEditPhoto(${index})">
                        <i class="bi bi-x"></i>
                    </button>
                `;
                editPhotoPreview.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
    };

    window.removeEditPhoto = (index) => {
        console.log('Fotoğraf kaldırılıyor:', index);
        window.editSelectedPhotos.splice(index, 1);
        updateEditPhotoPreview();
        if (window.editSelectedPhotos.length === 0) {
            editRemovePhotoBtn?.classList.add('d-none');
        }
    };

    window.removeExistingPhoto = (photoId) => {
        window.editExistingPhotoIds = window.editExistingPhotoIds.filter(id => id !== photoId);
        const photoElement = document.querySelector(`#existing-photo-${photoId}`);
        if (photoElement) {
            photoElement.remove();
        }
    };

    // Dropdown menüsünü başlat
    if (privacyButton) {
        const dropdown = new bootstrap.Dropdown(privacyButton);
        const editDropdown = new bootstrap.Dropdown(editPrivacyButton);

        // Dropdown öğelerine tıklama olayını dinle
        document.querySelectorAll('.dropdown-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const privacyId = item.dataset.privacyId;
                const privacyName = item.dataset.privacyName;
                const icon = item.querySelector('i').className;

                if (item.closest('#editPostModal')) {
                    editPrivacyInput.value = privacyId;
                    editPrivacyText.textContent = privacyName;
                    editPrivacyButton.querySelector('i').className = icon;
                    editDropdown.hide();
                } else {
                    privacyInput.value = privacyId;
                    privacyText.textContent = privacyName;
                    privacyButton.querySelector('i').className = icon;
                    dropdown.hide();
                }
            });
        });
    }

    // Fotoğraf yükleme işlemleri
    const photoInput = document.getElementById('photoInput');
    const photoPreview = document.getElementById('photoPreview');
    const removePhotoBtn = document.getElementById('removePhotoBtn');
    let selectedPhotos = [];

    photoInput?.addEventListener('change', (e) => {
        const files = Array.from(e.target.files);
        selectedPhotos = [...selectedPhotos, ...files];
        updatePhotoPreview();
        if (selectedPhotos.length > 0) {
            removePhotoBtn?.classList.remove('d-none');
        }
    });

    removePhotoBtn?.addEventListener('click', () => {
        selectedPhotos = [];
        updatePhotoPreview();
        photoInput.value = '';
        removePhotoBtn.classList.add('d-none');
    });

    function updatePhotoPreview() {
        if (!photoPreview) return;
        
        photoPreview.innerHTML = '';
        selectedPhotos.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const div = document.createElement('div');
                div.className = 'position-relative d-inline-block me-2 mb-2';
                div.innerHTML = `
                    <img src="${e.target.result}" class="img-thumbnail" style="width: 100px; height: 100px; object-fit: cover;">
                    <button type="button" class="btn btn-danger btn-sm position-absolute top-0 end-0 m-1" 
                            onclick="removePhoto(${index})">
                        <i class="bi bi-x"></i>
                    </button>
                `;
                photoPreview.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
    }

    window.removePhoto = (index) => {
        selectedPhotos.splice(index, 1);
        updatePhotoPreview();
        if (selectedPhotos.length === 0) {
            removePhotoBtn?.classList.add('d-none');
        }
    };
    
    // Gönderi listesini yükleme fonksiyonunu global kapsama taşı
    window.loadPosts = () => fetch('/post/list')
        .then(r => r.ok ? r.json() : Promise.reject())
        .then(p => {
            p.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            l.innerHTML = p.map(p => `
            <div class="card mb-3"><div class="card-body">
                <div class="d-flex align-items-center mb-2">
                    <img src="${p.profilePicture ? `/uploads/profile-photos/${p.userId}` : '/images/default-avatar.svg'}" 
                         class="rounded-circle me-2" style="width: 40px; height: 40px; object-fit: cover;">
                    <div class="flex-grow-1">
                        <a href="/profil/${p.username}" class="text-decoration-none text-dark">
                            <h6 class="card-subtitle text-muted mb-0">@${p.username}</h6>
                        </a>
                        <small class="text-muted">
                            <i class="bi ${getPrivacyIcon(p.privacyType)}"></i>
                            ${getPrivacyText(p.privacyType)}
                        </small>
                    </div>
                    ${p.isOwner ? `
                        <div class="dropdown">
                            <button class="btn btn-link text-muted" type="button" data-bs-toggle="dropdown">
                                <i class="bi bi-three-dots-vertical"></i>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="#" onclick="editPost(${p.postId}, '${p.content.replace(/'/g, "\\'")}', ${p.privacyType})">
                                    <i class="bi bi-pencil"></i> Düzenle
                                </a></li>
                                <li><a class="dropdown-item text-danger" href="#" onclick="deletePost(${p.postId})">
                                    <i class="bi bi-trash"></i> Sil
                                </a></li>
                            </ul>
                        </div>
                    ` : ''}
                </div>
                <p class="card-text">${p.content}</p>
                ${p.photos && p.photos.length > 0 ? `
                    <div class="row g-2 mb-3">
                        ${p.photos.length > 1 ? `
                            <div id="carousel-${p.postId}" class="carousel slide" data-bs-ride="carousel">
                                <div class="carousel-indicators">
                                    ${p.photos.map((_, index) => `
                                        <button type="button" data-bs-target="#carousel-${p.postId}" 
                                                data-bs-slide-to="${index}" ${index === 0 ? 'class="active"' : ''}></button>
                                    `).join('')}
                                </div>
                                <div class="carousel-inner">
                                    ${p.photos.map((photo, index) => `
                                        <div class="carousel-item ${index === 0 ? 'active' : ''}">
                                            <img src="/post/photo/${photo.photoId}" 
                                                 class="d-block w-100 rounded" 
                                                 style="max-height: 400px; object-fit: cover;">
                                        </div>
                                    `).join('')}
                                </div>
                                <button class="carousel-control-prev" type="button" data-bs-target="#carousel-${p.postId}" data-bs-slide="prev">
                                    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                                    <span class="visually-hidden">Önceki</span>
                                </button>
                                <button class="carousel-control-next" type="button" data-bs-target="#carousel-${p.postId}" data-bs-slide="next">
                                    <span class="carousel-control-next-icon" aria-hidden="true"></span>
                                    <span class="visually-hidden">Sonraki</span>
                                </button>
                            </div>
                        ` : `
                            <div class="col-12">
                                <img src="/post/photo/${p.photos[0].photoId}" 
                                     class="img-fluid rounded" 
                                     style="max-height: 400px; width: 100%; object-fit: cover;">
                            </div>
                        `}
                    </div>
                ` : ''}
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <small class="text-muted time-ago" data-timestamp="${p.createdAt}">${formatTimeAgo(p.createdAt)}</small>
                        <button class="btn btn-link text-muted" onclick="showComments(${p.postId})">
                            <i class="bi bi-chat"></i> <span class="comment-count">${p.commentCount || 0}</span>
                        </button>
                    </div>
                    <button class="btn btn-link text-danger like-button" data-post-id="${p.postId}" onclick="toggleLike(${p.postId})">
                        <i class="bi ${p.isLiked ? 'bi-heart-fill' : 'bi-heart'}"></i>
                        <span class="like-count">${p.likeCount || 0}</span>
                    </button>
                </div>
            </div></div>
        `).join('');
        })
        .catch(error => {
            console.error('Gönderiler yüklenirken hata oluştu:', error);
            l.innerHTML = `
                <div class="alert alert-warning" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    Gönderiler yüklenirken bir hata oluştu. Lütfen sayfayı yenileyin.
                </div>
            `;
        });

    f?.addEventListener('submit', e => {
        e.preventDefault();
        const c = f.querySelector('textarea[name="content"]').value.trim();
        if (!c && selectedPhotos.length === 0) return;

        const formData = new FormData();
        formData.append('content', c);
        formData.append('privacyType', f.querySelector('input[name="privacyType"]').value);
        selectedPhotos.forEach((file, index) => {
            formData.append('photos', file);
        });

        fetch('/post/create', { 
            method: 'POST', 
            body: formData 
        })
        .then(r => {
            if (r.ok) {
                f.reset();
                selectedPhotos = [];
                updatePhotoPreview();
                loadPosts();
            }
        })
        .catch(() => console.error('Gönderi oluşturulamadı'));
    });

    // Yorum formu işlemleri
    const commentForm = document.getElementById('commentForm');
    commentForm?.addEventListener('submit', e => {
        e.preventDefault();
        const content = commentForm.querySelector('textarea[name="content"]').value.trim();
        const postId = commentForm.querySelector('input[name="postId"]').value;
        
        if (!content) return;
        
        fetch('/comment/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ postId, content })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                commentForm.reset();
                loadComments(postId);
                updateCommentCount(postId, data.commentCount);
            }
        })
        .catch(error => console.error('Yorum oluşturulamadı:', error));
    });

    loadPosts();
});

function showComments(postId) {
    const modal = new bootstrap.Modal(document.getElementById('commentsModal'));
    document.getElementById('commentPostId').value = postId;
    loadComments(postId);
    modal.show();
}

function loadComments(postId) {
    fetch(`/comment/list/${postId}`)
        .then(response => response.json())
        .then(comments => {
            const commentsList = document.getElementById('commentsList');
            commentsList.innerHTML = comments.map(comment => `
                <div class="d-flex mb-3">
                    <div class="flex-shrink-0">
                        <img src="${comment.profilePicture ? `/uploads/profile-photos/${comment.userId}` : '/images/default-avatar.svg'}" 
                             class="rounded-circle" style="width: 40px; height: 40px; object-fit: cover;">
                    </div>
                    <div class="flex-grow-1 ms-3">
                        <div class="d-flex align-items-center justify-content-between">
                            <div>
                                <a href="/profil/${comment.username}" class="text-decoration-none text-dark">
                                    <h6 class="mb-0">@${comment.username}</h6>
                                </a>
                                <small class="text-muted time-ago" data-timestamp="${comment.createdAt}">
                                    ${formatTimeAgo(comment.createdAt)}
                                </small>
                            </div>
                            <div class="d-flex align-items-center">
                                <button class="btn btn-link text-muted p-0 me-2" onclick="showReplies(${comment.commentId})">
                                    <i class="bi bi-chat"></i> <span class="reply-count">${comment.replyCount || 0}</span>
                                </button>
                                <button class="btn btn-link text-danger p-0 comment-like-button me-2" 
                                        data-comment-id="${comment.commentId}" 
                                        onclick="toggleCommentLike(${comment.commentId})">
                                    <i class="bi ${comment.isLiked ? 'bi-heart-fill' : 'bi-heart'}"></i>
                                    <span class="comment-like-count">${comment.likeCount || 0}</span>
                                </button>
                                ${comment.isOwner ? `
                                    <div class="dropdown">
                                        <button class="btn btn-link text-muted p-0" type="button" data-bs-toggle="dropdown">
                                            <i class="bi bi-three-dots-vertical"></i>
                                        </button>
                                        <ul class="dropdown-menu dropdown-menu-end">
                                            <li><a class="dropdown-item text-danger" href="#" onclick="deleteComment(${comment.commentId})">
                                                <i class="bi bi-trash"></i> Sil
                                            </a></li>
                                        </ul>
                                    </div>
                                ` : ''}
                            </div>
                        </div>
                        <p class="mb-0">${comment.content}</p>
                        <div class="mt-2">
                            <button class="btn btn-link text-muted p-0" onclick="showReplyForm(${comment.commentId})">
                                <i class="bi bi-reply"></i> Yanıtla
                            </button>
                        </div>
                        <div id="replyForm-${comment.commentId}" class="mt-2 d-none">
                            <div class="input-group">
                                <input type="text" class="form-control" placeholder="Yanıtınızı yazın...">
                                <button class="btn btn-primary" onclick="submitReply(${comment.commentId})">Gönder</button>
                            </div>
                        </div>
                        <div id="replies-${comment.commentId}" class="mt-2"></div>
                    </div>
                </div>
            `).join('');
        })
        .catch(error => console.error('Yorumlar yüklenemedi:', error));
}

function showReplyForm(commentId) {
    const replyForm = document.getElementById(`replyForm-${commentId}`);
    replyForm.classList.remove('d-none');
    replyForm.querySelector('input').focus();
}

function submitReply(commentId) {
    const replyForm = document.getElementById(`replyForm-${commentId}`);
    const content = replyForm.querySelector('input').value.trim();
    
    if (!content) return;
    
    fetch(`/comment/reply/${commentId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ content })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            replyForm.querySelector('input').value = '';
            replyForm.classList.add('d-none');
            loadReplies(commentId);
            updateReplyCount(commentId, data.replyCount);
        }
    })
    .catch(error => console.error('Yanıt gönderilemedi:', error));
}

function showReplies(commentId) {
    const repliesContainer = document.getElementById(`replies-${commentId}`);
    if (repliesContainer.classList.contains('d-none')) {
        loadReplies(commentId);
        repliesContainer.classList.remove('d-none');
    } else {
        repliesContainer.classList.add('d-none');
    }
}

function loadReplies(commentId) {
    fetch(`/comment/replies/${commentId}`)
        .then(response => response.json())
        .then(replies => {
            const repliesContainer = document.getElementById(`replies-${commentId}`);
            repliesContainer.innerHTML = replies.map(reply => `
                <div class="d-flex mt-2">
                    <div class="flex-shrink-0">
                        <img src="${reply.profilePicture ? `/uploads/profile-photos/${reply.userId}` : '/images/default-avatar.svg'}" 
                             class="rounded-circle" style="width: 32px; height: 32px; object-fit: cover;">
                    </div>
                    <div class="flex-grow-1 ms-2">
                        <div class="d-flex align-items-center justify-content-between">
                            <div>
                                <a href="/profil/${reply.username}" class="text-decoration-none text-dark">
                                    <h6 class="mb-0 small">@${reply.username}</h6>
                                </a>
                                <small class="text-muted ms-2 time-ago" data-timestamp="${reply.createdAt}">
                                    ${formatTimeAgo(reply.createdAt)}
                                </small>
                            </div>
                            <button class="btn btn-link text-danger p-0 reply-like-button" 
                                    data-reply-id="${reply.replyId}" 
                                    onclick="toggleReplyLike(${reply.replyId})">
                                <i class="bi ${reply.isLiked ? 'bi-heart-fill' : 'bi-heart'}"></i>
                                <span class="reply-like-count">${reply.likeCount || 0}</span>
                            </button>
                        </div>
                        <p class="mb-0 small">${reply.content}</p>
                    </div>
                </div>
            `).join('');
        })
        .catch(error => console.error('Yanıtlar yüklenemedi:', error));
}

function toggleReplyLike(replyId) {
    fetch(`/comment/reply/like/${replyId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        const likeButton = document.querySelector(`.reply-like-button[data-reply-id="${replyId}"]`);
        const likeIcon = likeButton.querySelector('i');
        const likeCount = likeButton.querySelector('.reply-like-count');
        
        if (data.liked) {
            likeIcon.classList.remove('bi-heart');
            likeIcon.classList.add('bi-heart-fill');
        } else {
            likeIcon.classList.remove('bi-heart-fill');
            likeIcon.classList.add('bi-heart');
        }
        
        likeCount.textContent = data.likeCount;
    })
    .catch(error => {
        console.error('Beğeni işlemi başarısız:', error);
    });
}

function updateReplyCount(commentId, count) {
    const replyCount = document.querySelector(`[onclick="showReplies(${commentId})"] .reply-count`);
    if (replyCount) {
        replyCount.textContent = count;
    }
}

function updateCommentCount(postId, count) {
    const commentCount = document.querySelector(`[onclick="showComments(${postId})"] .comment-count`);
    if (commentCount) {
        commentCount.textContent = count;
    }
}

function toggleLike(postId) {
    fetch(`/post/like/${postId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        const likeButton = document.querySelector(`.like-button[data-post-id="${postId}"]`);
        const likeIcon = likeButton.querySelector('i');
        const likeCount = likeButton.querySelector('.like-count');
        
        if (data.liked) {
            likeIcon.classList.remove('bi-heart');
            likeIcon.classList.add('bi-heart-fill');
        } else {
            likeIcon.classList.remove('bi-heart-fill');
            likeIcon.classList.add('bi-heart');
        }
        
        likeCount.textContent = data.likeCount;
    })
    .catch(error => {
        console.error('Beğeni işlemi başarısız:', error);
    });
}

function toggleCommentLike(commentId) {
    fetch(`/comment/like/${commentId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        const likeButton = document.querySelector(`.comment-like-button[data-comment-id="${commentId}"]`);
        const likeIcon = likeButton.querySelector('i');
        const likeCount = likeButton.querySelector('.comment-like-count');
        
        if (data.liked) {
            likeIcon.classList.remove('bi-heart');
            likeIcon.classList.add('bi-heart-fill');
        } else {
            likeIcon.classList.remove('bi-heart-fill');
            likeIcon.classList.add('bi-heart');
        }
        
        likeCount.textContent = data.likeCount;
    })
    .catch(error => {
        console.error('Beğeni işlemi başarısız:', error);
    });
}

function getPrivacyIcon(privacyType) {
    switch (privacyType) {
        case 1: return 'bi-globe';
        case 2: return 'bi-people';
        case 3: return 'bi-lock';
        default: return 'bi-globe';
    }
}

function getPrivacyText(privacyType) {
    switch (privacyType) {
        case 1: return 'Herkese Açık';
        case 2: return 'Sadece Takipçiler';
        case 3: return 'Sadece Ben';
        default: return 'Herkese Açık';
    }
}

// Gönderi düzenleme fonksiyonu
function editPost(postId, content, privacyType) {
    const modal = new bootstrap.Modal(document.getElementById('editPostModal'));
    document.getElementById('editPostId').value = postId;
    document.getElementById('editPostContent').value = content;
    document.getElementById('editPrivacyType').value = privacyType;
    
    // Gizlilik ayarını güncelle
    const privacyText = document.querySelector('.edit-privacy-text');
    const privacyIcon = document.querySelector('#editPrivacyDropdown i');
    privacyText.textContent = getPrivacyText(privacyType);
    privacyIcon.className = `bi ${getPrivacyIcon(privacyType)}`;
    
    // Mevcut fotoğrafları yükle
    window.editSelectedPhotos = [];
    window.editExistingPhotoIds = [];
    updateEditPhotoPreview();
    editExistingPhotos.innerHTML = '';
    
    fetch(`/post/${postId}`)
        .then(response => response.json())
        .then(data => {
            if (data.photos && data.photos.length > 0) {
                data.photos.forEach(photo => {
                    window.editExistingPhotoIds.push(photo.photoId);
                    const div = document.createElement('div');
                    div.id = `existing-photo-${photo.photoId}`;
                    div.className = 'position-relative d-inline-block me-2 mb-2';
                    div.innerHTML = `
                        <img src="/post/photo/${photo.photoId}" class="img-thumbnail" style="width: 100px; height: 100px; object-fit: cover;">
                        <button type="button" class="btn btn-danger btn-sm position-absolute top-0 end-0 m-1" 
                                onclick="removeExistingPhoto(${photo.photoId})">
                            <i class="bi bi-x"></i>
                        </button>
                    `;
                    editExistingPhotos.appendChild(div);
                });
            }
        })
        .catch(error => console.error('Gönderi bilgileri yüklenemedi:', error));
    
    modal.show();
}

// Gönderi güncelleme fonksiyonu
function updatePost() {
    const postId = document.getElementById('editPostId').value;
    const content = document.getElementById('editPostContent').value.trim();
    const privacyType = parseInt(document.getElementById('editPrivacyType').value);
    
    if (!content) {
        alert('Gönderi içeriği boş olamaz');
        return;
    }
    
    console.log('Gönderi güncelleniyor:', {
        postId,
        content,
        privacyType,
        selectedPhotos: window.editSelectedPhotos.length,
        existingPhotoIds: window.editExistingPhotoIds
    });
    
    const formData = new FormData();
    formData.append('content', content);
    formData.append('privacyType', privacyType);
    
    // Mevcut fotoğraf ID'lerini dizi olarak gönder
    if (window.editExistingPhotoIds.length > 0) {
        window.editExistingPhotoIds.forEach(id => {
            formData.append('existingPhotoIds', id);
        });
    }
    
    // Yeni fotoğrafları ekle
    window.editSelectedPhotos.forEach((file, index) => {
        console.log('Fotoğraf ekleniyor:', file.name);
        formData.append('photos', file);
    });

    // FormData içeriğini kontrol et
    for (let pair of formData.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
    }
    
    fetch(`/post/update/${postId}`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Gönderi güncellenirken bir hata oluştu');
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('editPostModal'));
            modal.hide();
            loadPosts();
        } else {
            alert(data.message || 'Gönderi güncellenirken bir hata oluştu');
        }
    })
    .catch(error => {
        console.error('Gönderi güncellenemedi:', error);
        alert(error.message || 'Gönderi güncellenirken bir hata oluştu');
    });
}

// Gönderi silme fonksiyonu
function deletePost(postId) {
    if (confirm('Bu gönderiyi silmek istediğinizden emin misiniz?')) {
        fetch(`/post/delete/${postId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                loadPosts();
            }
        })
        .catch(error => console.error('Gönderi silinemedi:', error));
    }
}

// Yorum silme fonksiyonu
function deleteComment(commentId) {
    if (confirm('Bu yorumu silmek istediğinizden emin misiniz?')) {
        fetch(`/comment/delete/${commentId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const postId = document.getElementById('commentPostId').value;
                loadComments(postId);
                updateCommentCount(postId, data.commentCount);
            } else {
                alert(data.message || 'Yorum silinirken bir hata oluştu');
            }
        })
        .catch(error => {
            console.error('Yorum silinemedi:', error);
            alert('Yorum silinirken bir hata oluştu');
        });
    }
} 