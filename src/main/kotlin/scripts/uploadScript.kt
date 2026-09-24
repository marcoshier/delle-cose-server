package scripts

import org.intellij.lang.annotations.Language


fun uploadScript(projectName: String): String {

    val jsProject = projectName
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\r", "")
        .replace("\n", "")

    @Language("js")
    val js = """
        const PROJECT = '$jsProject';
        let processingCheckInterval;
        let wasProcessing = false;
        const renderedRows = {};

        const STAGE_LABELS = {
            QUEUED: 'queued',
            REENCODING: 'reencoding',
            THUMBNAIL: 'thumbnail',
            DONE: 'done ✓',
            FAILED: 'failed ✗',
            CANCELLED: 'cancelled'
        };

        document.addEventListener('DOMContentLoaded', function () {
            checkProcessingStatus();
            processingCheckInterval = setInterval(checkProcessingStatus, 2000);

            const fileInput = document.getElementById('fileInput');
            if (fileInput) fileInput.addEventListener('change', handleFileUpload);

            const cancelBtn = document.getElementById('cancelBtn');
            if (cancelBtn) cancelBtn.addEventListener('click', () => cancelProcessing(PROJECT));
        });

        async function checkProcessingStatus() {
            try {
                const res = await fetch('/media-processing-status/' + encodeURIComponent(PROJECT));
                if (!res.ok) return;
                renderProcessing(await res.json());
            } catch (e) {
                console.error('Error checking processing status:', e);
            }
        }

        function renderProcessing(data) {
            const box = document.getElementById('processingStatus');
            const cancelBtn = document.getElementById('cancelBtn');
            const files = data.files || [];

            if (data.active) {
                box.style.display = 'block';
                if (cancelBtn) cancelBtn.style.display = '';
                updateHeader(data);
                updateRows(files);
                wasProcessing = true;
                return;
            }

            if (wasProcessing) {
                box.style.display = 'block';
                if (cancelBtn) cancelBtn.style.display = 'none';
                updateHeader(data);
                updateRows(files);
                wasProcessing = false;
                clearInterval(processingCheckInterval);
                setTimeout(() => location.reload(), 1800);
            } else {
                box.style.display = 'none';
            }
        }

        function updateHeader(data) {
            const files = data.files || [];
            const total = files.length;
            const done = files.filter(f => f.stage === 'DONE').length;
            const text = document.getElementById('processingText');
            const overall = document.getElementById('processingOverall');

            if (data.active) {
                text.textContent = 'Convertendo ' + done + ' / ' + total + ' file';
            } else if (files.some(f => f.stage === 'CANCELLED')) {
                text.textContent = 'Annullato';
            } else if (files.some(f => f.stage === 'FAILED')) {
                text.textContent = 'Concluso con errori';
            } else {
                text.textContent = 'Completato — ' + total + ' file';
            }
            overall.textContent = (data.overallPercent || 0) + '%';
        }

        function updateRows(files) {
            const container = document.getElementById('processingFiles');
            const seen = {};

            files.forEach(f => {
                seen[f.fileName] = true;
                let refs = renderedRows[f.fileName];
                if (!refs) {
                    refs = createRow(f.fileName);
                    container.appendChild(refs.row);
                    renderedRows[f.fileName] = refs;
                }
                refs.stage.textContent = STAGE_LABELS[f.stage] || f.stage.toLowerCase();
                refs.fill.style.width = f.percent + '%';
                refs.pct.textContent = f.percent + '%';
                refs.row.className = 'pf-row pf-' + f.stage.toLowerCase();
                refs.stage.title = f.error || '';
            });

            Object.keys(renderedRows).forEach(name => {
                if (!seen[name]) {
                    renderedRows[name].row.remove();
                    delete renderedRows[name];
                }
            });
        }

        function createRow(fileName) {
            const row = document.createElement('div');
            row.className = 'pf-row';

            const name = document.createElement('span');
            name.className = 'pf-name';
            name.textContent = fileName;

            const stage = document.createElement('span');
            stage.className = 'pf-stage';

            const bar = document.createElement('div');
            bar.className = 'pf-bar';
            const fill = document.createElement('div');
            fill.className = 'pf-fill';
            bar.appendChild(fill);

            const pct = document.createElement('span');
            pct.className = 'pf-pct';

            row.append(name, stage, bar, pct);
            return { row, fill, stage, pct };
        }

        async function cancelProcessing(projectName) {
            const btn = document.getElementById('cancelBtn');
            if (btn) { btn.disabled = true; btn.textContent = 'Cancelling…'; }
            try {
                const res = await fetch('/cancel-processing/' + encodeURIComponent(projectName), { method: 'POST' });
                const result = await res.json();
                
                if (!result.success) {
                    alert('Failed to cancel: ' + (result.message || 'unknown'));
                    if (btn) { btn.disabled = false; btn.textContent = 'Cancel'; }
                }
            } catch (e) {
                console.error('Error cancelling processing:', e);
                alert('Error cancelling processing');
                if (btn) { btn.disabled = false; btn.textContent = 'Cancel'; }
            }
        }

        async function handleFileUpload(event) {
            const files = event.target.files;
            if (files.length === 0) return;

            const formData = new FormData();
            formData.append('folderName', PROJECT);
            for (let i = 0; i < files.length; i++) formData.append('files', files[i]);

            const progressContainer = document.getElementById('uploadProgress');
            const progressFill = document.getElementById('progressFill');
            const uploadStatus = document.getElementById('uploadStatus');

            progressContainer.style.display = 'block';
            uploadStatus.textContent = 'Uploading ' + files.length + ' file(s)...';
            progressFill.style.width = '0%';

            const xhr = new XMLHttpRequest();
            const abort = () => xhr.abort();
            window.addEventListener('beforeunload', abort);

            xhr.upload.addEventListener('progress', function (e) {
                if (e.lengthComputable) progressFill.style.width = ((e.loaded / e.total) * 100) + '%';
            });

            xhr.onload = function () {
                window.removeEventListener('beforeunload', abort);
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

            xhr.onerror = function () {
                window.removeEventListener('beforeunload', abort);
                uploadStatus.textContent = 'Upload failed: Network error';
            };

            xhr.open('POST', '/upload-media');
            xhr.send(formData);
        }

        async function deleteMedia(folderName, filename) {
            if (!confirm('Are you sure you want to delete this file?')) return;

            const formData = new FormData();
            formData.append('folderName', folderName);
            formData.append('filename', filename);

            try {
                const res = await fetch('/delete-media', { method: 'DELETE', body: formData });
                const result = await res.json();
                if (result.success === 'true') location.reload();
                else alert('Delete failed: ' + result.message);
            } catch (e) {
                alert('Delete failed: ' + e.message);
            }
        }
    """.trimIndent()

    @Language("html")
    val script = """
        <script>
        $js
        </script>
    """.trimIndent()

    return script
}