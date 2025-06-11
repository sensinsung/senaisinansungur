function formatTimeAgo(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    const diffInHours = Math.floor(diffInMinutes / 60);
    const diffInDays = Math.floor(diffInHours / 24);
    const diffInMonths = Math.floor(diffInDays / 30);

    if (diffInSeconds < 60) {
        return 'az önce';
    } else if (diffInSeconds < 120) {
        return '1 dakika önce';
    } else if (diffInMinutes < 60) {
        return `${diffInMinutes} dakika önce`;
    } else if (diffInHours < 24) {
        const hours = Math.floor(diffInHours);
        const minutes = diffInMinutes % 60;
        if (minutes === 0) {
            return `${hours} saat önce`;
        }
        return `${hours} saat ${minutes} dakika önce`;
    } else if (diffInDays === 1) {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `dün ${hours}:${minutes}`;
    } else if (diffInDays < 7) {
        return `${diffInDays} gün önce`;
    } else if (diffInDays < 30) {
        const weeks = Math.floor(diffInDays / 7);
        return `${weeks} hafta önce`;
    } else if (diffInMonths < 12) {
        return `${diffInMonths} ay önce`;
    } else {
        const day = date.getDate().toString().padStart(2, '0');
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const year = date.getFullYear();
        return `${day}.${month}.${year}`;
    }
}

// Sayfa yüklendiğinde tüm zaman etiketlerini güncelle
document.addEventListener('DOMContentLoaded', function() {
    const timeElements = document.querySelectorAll('.time-ago');
    timeElements.forEach(element => {
        const timestamp = element.getAttribute('data-timestamp');
        if (timestamp) {
            element.textContent = formatTimeAgo(timestamp);
        }
    });
}); 