class ApiService {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async request(endpoint, options = {}) {
        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                /*headers: {
                    'Content-Type': 'application/json',
                    ...options.headers,
                },*/
                ...options,
            });

            

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const contentType = response.headers.get('content-type');
            return contentType?.includes('application/json') 
                ? response.json() 
                : response.text();
        } catch (error) {
            console.error(`API request failed: ${endpoint}`, error);
            throw error;
        }
    }

    uploadFile(file) {
        const formData = new FormData();
        formData.append('file', file);
        
        return this.request('/upload', {
            method: 'POST',
            body: formData,
        });
    }

    getFiles() {
        return this.request('');
    }

    generateDownloadUrl(fileId) {
        return this.request('/get-url', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `uuid=${fileId}`,
        });
    }

    updateDownloadCount(fileId) {
        return this.request('/update-download-count', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `uuid=${fileId}`,
        });
    }
}

class UIManager {
    constructor() {
        this.elements = this.initializeElements();
    }

    initializeElements() {
        return {
            uploadForm: document.getElementById('uploadForm'),
            fileInput: document.getElementById('fileInput'),
            filesList: document.getElementById('filesList'),
            uploadStatus: document.getElementById('uploadStatus'),
            uploadBtn: document.querySelector('.upload-btn'),
        };
    }

    showStatus(message, type = 'info') {
        const { uploadStatus } = this.elements;
        uploadStatus.textContent = message;
        uploadStatus.className = `status ${type}`;
    }

    clearStatus() {
        this.showStatus('', '');
    }

    setLoading(loading) {
        const { uploadBtn, fileInput } = this.elements;
        
        uploadBtn.disabled = loading;
        uploadBtn.textContent = loading ? 'Загрузка...' : 'Загрузить';
        
        if (loading) {
            document.body.classList.add('loading');
        } else {
            document.body.classList.remove('loading');
        }
    }

    renderFilesList(files = []) {
        const { filesList } = this.elements;
        
        if (!files.length) {
            filesList.innerHTML = '<p class="no-files">Файлы не загружены</p>';
            return;
        }

        filesList.innerHTML = files.map(file => `
            <div class="file-item">
                <span class="file-name">${file.originalFileName || file.id}</span>
                <button class="get-link-btn" data-file-id="${file.id}" data-file-name="${file.originalFileName}">
                    Скачать файл
                </button>
            </div>
        `).join('');
    }

    showFilesListError() {
        const { filesList } = this.elements;
        filesList.innerHTML = '<p class="error">Ошибка загрузки списка файлов</p>';
    }

    resetUploadForm() {
        this.elements.uploadForm.reset();
    }

    validateFileInput() {
        const file = this.elements.fileInput.files[0];
        if (!file) {
            this.showStatus('Пожалуйста, выберите файл', 'error');
            return null;
        }
        return file;
    }
}

class FileDownloadService {
    constructor(apiService, uiManager) {
        this.apiService = apiService;
        this.uiManager = uiManager;
    }

    async downloadFile(fileId, fileName) {
        try {
            this.uiManager.showStatus('Начинаем скачивание...', 'loading');
            
            const fileUrl = await this.apiService.generateDownloadUrl(fileId);
            await this.downloadFileFromUrl(fileUrl, fileName);
            await this.apiService.updateDownloadCount(fileId);
            
            this.uiManager.showStatus('Файл успешно скачан!', 'success');
        } catch (error) {
            console.error('Download error:', error);
            this.uiManager.showStatus('Ошибка скачивания файла', 'error');
        }
    }

    async downloadFileFromUrl(fileUrl, fileName) {
        const response = await fetch(fileUrl);
        
        if (!response.ok) {
            throw new Error('Ошибка загрузки файла');
        }
        
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = fileName || 'download';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        window.URL.revokeObjectURL(downloadUrl);
    }
}

// Главный класс-координатор
class FileService {
    constructor(baseUrl = 'http://localhost:8081/api/v1/files') {
        this.apiService = new ApiService(baseUrl);
        this.uiManager = new UIManager();
        this.downloadService = new FileDownloadService(this.apiService, this.uiManager);
        
        this.initializeEventListeners();
        this.loadFilesList();
    }

    initializeEventListeners() {
        const { uploadForm, fileInput, filesList } = this.uiManager.elements;
        
        uploadForm.addEventListener('submit', (e) => this.handleUpload(e));
        fileInput.addEventListener('change', () => this.uiManager.clearStatus());
        
        // Делегирование событий для динамических элементов
        filesList.addEventListener('click', (e) => this.handleFileListClick(e));
    }

    async handleUpload(event) {
        event.preventDefault();
        
        const file = this.uiManager.validateFileInput();
        if (!file) return;

        await this.uploadFile(file);
    }

    async uploadFile(file) {
        try {
            this.uiManager.setLoading(true);
            this.uiManager.showStatus('Загрузка файла...', 'loading');

            const result = await this.apiService.uploadFile(file);
            
            this.uiManager.showStatus('Файл успешно загружен!', 'success');
            this.uiManager.resetUploadForm();
            await this.loadFilesList();
            
        } catch (error) {
            console.error('Upload error:', error);
            this.uiManager.showStatus(`Ошибка загрузки: ${error.message}`, 'error');
        } finally {
            this.uiManager.setLoading(false);
        }
    }

    async loadFilesList() {
        try {
            const files = await this.apiService.getFiles();
            this.uiManager.renderFilesList(files);
        } catch (error) {
            console.error('Load files error:', error);
            this.uiManager.showFilesListError();
        }
    }

    handleFileListClick(event) {
        if (event.target.classList.contains('get-link-btn')) {
            const { fileId, fileName } = event.target.dataset;
            this.downloadService.downloadFile(fileId, fileName);
        }
    }
}

// Инициализация приложения
class App {
    constructor() {
        this.fileService = new FileService();
        this.initializeErrorHandling();
    }

    initializeErrorHandling() {
        window.addEventListener('error', (event) => {
            console.error('Global error:', event.error);
            this.fileService.uiManager.showStatus('Произошла непредвиденная ошибка', 'error');
        });

        window.addEventListener('unhandledrejection', (event) => {
            console.error('Unhandled promise rejection:', event.reason);
            this.fileService.uiManager.showStatus('Произошла ошибка при выполнении операции', 'error');
        });
    }
}

// Запуск приложения
const app = new App();