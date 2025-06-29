package com.marcoshier.components

fun uploadScript(projectName: String) = """
        <script>
            const uploadContainer = document.getElementById('uploadContainer');
            const fileInput = document.getElementById('fileInput');
            const uploadProgress = document.getElementById('uploadProgress');
            const progressFill = document.getElementById('progressFill');
            const uploadStatus = document.getElementById('uploadStatus');
            
            let currentUpload = null;
            
           
            fileInput.addEventListener('change', (e) => {
                const files = Array.from(e.target.files);
                uploadFiles(files);
            });
            
            
            window.addEventListener('beforeunload', (e) => {
                if (currentUpload) {
                    e.preventDefault();
                    e.returnValue = 'Upload in progress. Are you sure you want to leave?';
                    return e.returnValue;
                }
            });
            
            async function uploadFiles(files) {
                if (files.length === 0) return;
                
                if (currentUpload) {
                    uploadStatus.textContent = 'Another upload is in progress...';
                    return;
                }
                
                uploadProgress.style.display = 'block';
                progressFill.style.width = '0%';
                uploadStatus.textContent = 'Preparing upload...';
                
                const formData = new FormData();
                formData.append('folderName', '$projectName');
                
                files.forEach((file, index) => {
                    formData.append('files', file);
                });
                
                try {
                    const xhr = new XMLHttpRequest();
                    currentUpload = xhr;
                    
                    xhr.upload.addEventListener('progress', (e) => {
                        if (e.lengthComputable) {
                            const percentComplete = (e.loaded / e.total) * 100;
                            const loadedMB = (e.loaded / 1024 / 1024).toFixed(1);
                            const totalMB = (e.total / 1024 / 1024).toFixed(1);
                            
                            progressFill.style.width = percentComplete + '%';
                            uploadStatus.textContent = `Caricamento: Math.round(percentComplete), loadedMB}/\${'$'}{totalMB} MB)`;
                        }
                    });
                    
                    
                    xhr.addEventListener('load', () => {
                        currentUpload = null;
                        if (xhr.status === 200) {
                            progressFill.style.width = '100%';
                            uploadStatus.textContent = 'Caricamento completato! Ricaricando la pagina...';
                            setTimeout(() => {
                                window.location.reload();
                            }, 1500);
                        } else {
                            uploadStatus.textContent = 'Errore durante il caricamento';
                            setTimeout(() => {
                                uploadProgress.style.display = 'none';
                            }, 3000);
                        }
                    });
                    
                    xhr.addEventListener('error', () => {
                        currentUpload = null;
                        uploadStatus.textContent = 'Errore durante il caricamento';
                        setTimeout(() => {
                            uploadProgress.style.display = 'none';
                        }, 3000);
                    });
                    
                    xhr.addEventListener('abort', () => {
                        currentUpload = null;
                        uploadStatus.textContent = 'Caricamento annullato';
                        setTimeout(() => {
                            uploadProgress.style.display = 'none';
                        }, 3000);
                    });
                    
                    uploadStatus.textContent = 'Caricamento in corso...';
                    xhr.open('POST', '/upload-media');
                    xhr.send(formData);
                    
                } catch (error) {
                    currentUpload = null;
                    uploadStatus.textContent = 'Errore durante il caricamento';
                    uploadProgress.style.display = 'none';
                }
            }
            
       
            function cancelUpload() {
                if (currentUpload) {
                    currentUpload.abort();
                    currentUpload = null;
                    uploadStatus.textContent = 'Caricamento annullato';
                    uploadProgress.style.display = 'none';
                }
            }
            </script>
""".trimIndent()