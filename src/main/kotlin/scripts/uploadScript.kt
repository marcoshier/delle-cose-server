fun uploadScript(projectName: String): String {
    return """
        <script>
        let processingCheckInterval;
        let wasProcessing = false;
        
        document.addEventListener('DOMContentLoaded', function() {
            checkProcessingStatus();
            processingCheckInterval = setInterval(checkProcessingStatus, 3000);
            
            const fileInput = document.getElementById('fileInput');
            if (fileInput) {
                fileInput.addEventListener('change', handleFileUpload);
            }
        });
        
        async function checkProcessingStatus() {
            try {
                const response = await fetch('/media-processing-status');
                const data = await response.json();
                
                const isProcessing = data.processing.includes('$projectName');
                const processingStatus = document.getElementById('processingStatus');
                
                if (isProcessing) {
                    processingStatus.style.display = 'block';
                    document.getElementById('processingText').textContent = 
                        'Processing media for $projectName...';
                    wasProcessing = true;
                } else {
                    processingStatus.style.display = 'none';
                    if (wasProcessing) {
                        location.reload();
                    }
                }
            } catch (error) {
                console.error('Error checking processing status:', error);
            }
        }
        
        async function cancelProcessing(projectName) {
            try {
                const response = await fetch('/cancel-processing/' + encodeURIComponent(projectName), {
                    method: 'POST'
                });
                const result = await response.json();
                
                if (result.success) {
                    document.getElementById('processingStatus').style.display = 'none';
                    wasProcessing = false;
                } else {
                    alert('Failed to cancel processing: ' + result.message);
                }
            } catch (error) {
                console.error('Error cancelling processing:', error);
                alert('Error cancelling processing');
            }
        }
        
        async function handleFileUpload(event) {
            const files = event.target.files;
            if (files.length === 0) return;
            
            const formData = new FormData();
            formData.append('folderName', '$projectName');
            
            for (let i = 0; i < files.length; i++) {
                formData.append('files', files[i]);
            }
            
            const progressContainer = document.getElementById('uploadProgress');
            const progressFill = document.getElementById('progressFill');
            const uploadStatus = document.getElementById('uploadStatus');
            
            progressContainer.style.display = 'block';
            uploadStatus.textContent = 'Uploading ' + files.length + ' file(s)...';
            progressFill.style.width = '0%';
            
            try {
                const xhr = new XMLHttpRequest();
                
                let activeUpload = null;

                activeUpload = xhr;

                window.addEventListener('beforeunload', function() {
                    if (activeUpload) {
                        activeUpload.abort();
                        activeUpload = null;
                    }
                });
                
                xhr.upload.addEventListener('progress', function(e) {
                    if (e.lengthComputable) {
                        const percentComplete = (e.loaded / e.total) * 100;
                        progressFill.style.width = percentComplete + '%';
                    }
                });
                
                xhr.onload = function() {
                    if (xhr.status === 200) {
                        try {
                            const result = JSON.parse(xhr.responseText);
                            
                            if (result.success === 'true') {
                                uploadStatus.textContent = result.message;
                                progressFill.style.width = '100%';
                                
                                setTimeout(() => {
                                    progressContainer.style.display = 'none';
                                    progressFill.style.width = '0%';
                                    event.target.value = '';
                                }, 2000);
                                
                                checkProcessingStatus();
                                
                            } else {
                                uploadStatus.textContent = 'Upload failed: ' + (result.error || 'Unknown error');
                            }
                        } catch (e) {
                            uploadStatus.textContent = 'Upload failed: Invalid response';
                        }
                    } else {
                        uploadStatus.textContent = 'Upload failed: Server error';
                    }
                };
                
                xhr.onerror = function() {
                    uploadStatus.textContent = 'Upload failed: Network error';
                };
                
                xhr.open('POST', '/upload-media');
                xhr.send(formData);
                
            } catch (error) {
                uploadStatus.textContent = 'Upload failed: ' + error.message;
            }
        }
        
        async function deleteMedia(folderName, filename) {
            if (!confirm('Are you sure you want to delete this file?')) {
                return;
            }
            
            const formData = new FormData();
            formData.append('folderName', folderName);
            formData.append('filename', filename);
            
            try {
                const response = await fetch('/delete-media', {
                    method: 'DELETE',
                    body: formData
                });
                
                const result = await response.json();
                
                if (result.success === 'true') {
                    location.reload();
                } else {
                    alert('Delete failed: ' + result.message);
                }
            } catch (error) {
                alert('Delete failed: ' + error.message);
            }
        }
        </script>
    """.trimIndent()
}