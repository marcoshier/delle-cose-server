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
                            if(percentComplete != 100) {
                                uploadStatus.textContent = `Caricamento:` + Math.round(percentComplete) + `% (` + loadedMB + `/` + totalMB + `) MB`;
                            } else {
                                uploadStatus.style.color = 'green';
                                uploadStatus.textContent = `Conversione in corso. Non chiudere la pagina...`;
                            }
                            
                        }
                    });
                    
                    
                    xhr.addEventListener('load', async () => {
                        currentUpload = null;
                        
                        if (xhr.status === 200) {
                            try {
                                const response = JSON.parse(xhr.responseText);
                                
                               
                                if (response.error) {
                                    progressFill.style.width = '0%';
                                    uploadStatus.textContent = `Errore: \${'$'}{response.error}`;
                                    uploadStatus.style.color = 'red';
                                    setTimeout(() => {
                                        uploadProgress.style.display = 'none';
                                        uploadStatus.style.color = '';
                                    }, 5000);
                                    return;
                                }
                                
                                if (response.rejectedFiles && response.rejectedFiles.length > 0) {
                                    progressFill.style.width = '100%';
                                    uploadStatus.textContent = `Completato con avvisi: \${'$'}{response.message}`;
                                    uploadStatus.style.color = 'orange';
                                    
                                    
                                    const rejectedInfo = response.rejectedFiles.join(', ');
                                    setTimeout(() => {
                                        uploadStatus.textContent = `File rifiutati: \${'$'}{rejectedInfo}`;
                                    }, 2000);
                                    
                                    setTimeout(() => {
                                        window.location.reload();
                                    }, 5000);
                                    return;
                                }
                                
                                
                                progressFill.style.width = '100%';
                                uploadStatus.textContent = 'Conversione in corso. Non chiudere la pagina...';
                                uploadStatus.style.color = 'green';
                                setTimeout(() => {
                                    window.location.reload();
                                }, 1500);
                                
                            } catch (parseError) {
                           
                                progressFill.style.width = '0%';
                                uploadStatus.textContent = 'Errore: Risposta del server non valida';
                                uploadStatus.style.color = 'red';
                                setTimeout(() => {
                                    uploadProgress.style.display = 'none';
                                    uploadStatus.style.color = '';
                                }, 5000);
                            }
                        } else {
                        
                            progressFill.style.width = '0%';
                            uploadStatus.textContent = `Errore HTTP: \${'$'}{xhr.status}`;
                            uploadStatus.style.color = 'red';
                            setTimeout(() => {
                                uploadProgress.style.display = 'none';
                                uploadStatus.style.color = '';
                            }, 5000);
                        }
                    });
                    
                    xhr.addEventListener('error', () => {
                        currentUpload = null;
                        progressFill.style.width = '0%';
                        uploadStatus.textContent = 'Errore di connessione durante il caricamento';
                        uploadStatus.style.color = 'red';
                        setTimeout(() => {
                            uploadProgress.style.display = 'none';
                            uploadStatus.style.color = '';
                        }, 5000);
                    });
                    
                    xhr.addEventListener('abort', () => {
                        currentUpload = null;
                        progressFill.style.width = '0%';
                        uploadStatus.textContent = 'Caricamento annullato';
                        uploadStatus.style.color = 'orange';
                        setTimeout(() => {
                            uploadProgress.style.display = 'none';
                            uploadStatus.style.color = '';
                        }, 3000);
                    });
                    
                    uploadStatus.textContent = 'Caricamento in corso...';
                    xhr.open('POST', '/upload-media');
                    xhr.send(formData);
                    
                } catch (error) {
                    currentUpload = null;
                    progressFill.style.width = '0%';
                    uploadStatus.textContent = 'Errore durante il caricamento';
                    uploadStatus.style.color = 'red';
                    uploadProgress.style.display = 'none';
                }
            }
           async function deleteMedia(folderName, filename) {
                if (!confirm(`Sei sicuro di voler eliminare? Questa azione non può essere annullata.`)) {
                    return;
                }
                
                const mediaItemId = 'media-' + filename.replace(/[^a-zA-Z0-9]/g, '_');
                const mediaItem = document.getElementById(mediaItemId);
                
                try {
                    // Show loading state
                    if (mediaItem) {
                        mediaItem.classList.add('deleting');
                    }
                    
                    // Use FormData instead of JSON
                    const formData = new FormData();
                    formData.append('folderName', folderName);
                    formData.append('filename', filename);
                    
                    const response = await fetch('/delete-media', {
                        method: 'DELETE',
                        body: formData  // No Content-Type header needed
                    });
                    
                    const result = await response.json();
                    
                    if (response.ok && result.success === "true") {
                        // Remove the item from DOM with animation
                        if (mediaItem) {
                            mediaItem.style.transition = 'opacity 0.3s ease';
                            mediaItem.style.opacity = '0';
                            setTimeout(() => {
                                mediaItem.remove();
                            }, 300);
                        }
                        
                        // Update stats
                        updateMediaStats();
                        
                    } else {
                        // Remove loading state
                        if (mediaItem) {
                            mediaItem.classList.remove('deleting');
                        }
                        alert(`Errore durante l'eliminazione`);
                    }
                    
                } catch (error) {
                    // Remove loading state
                    if (mediaItem) {
                        mediaItem.classList.remove('deleting');
                    }
                    alert('Errore di connessione durante l\'eliminazione');
                    console.error('Delete error:', error);
                }
            }
            
            function updateMediaStats() {
                const remainingItems = document.querySelectorAll('.media-item:not(.deleting)').length;
                const statsElement = document.querySelector('.stats');
                if (statsElement) {
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
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