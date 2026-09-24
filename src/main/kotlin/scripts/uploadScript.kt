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
        const ALLOWED = ['jpg','jpeg','png','gif','bmp','webp','mp4','mov','avi','mkv'];
        const STAGE_LABELS = {
            QUEUED: 'queued', REENCODING: 'reencoding', THUMBNAIL: 'thumbnail',
            DONE: 'done ✓', FAILED: 'failed ✗', CANCELLED: 'cancelled'
        };

        let phase = 'idle';        // idle | uploading | awaiting | processing
        let pollInterval = null;
        let awaitingSince = 0;
        let activeXhr = null;
        const upRows = {};   
        const procRows = {}; 
        let uploadMeta = []; 

        document.addEventListener('DOMContentLoaded', function () {
            checkProcessingStatus();
            pollInterval = setInterval(checkProcessingStatus, 2000);

            const fileInput = document.getElementById('fileInput');
            if (fileInput) fileInput.addEventListener('change', handleFileUpload);

            const cancelBtn = document.getElementById('cancelBtn');
            if (cancelBtn) cancelBtn.addEventListener('click', onCancelClick);
        });


        function createRow(displayName) {
            const row = document.createElement('div');
            row.className = 'pf-row';
            const name = document.createElement('span');
            name.className = 'pf-name';
            name.textContent = displayName;  
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

        function clearUploadRows() {
            const c = document.getElementById('uploadFiles');
            if (c) c.innerHTML = '';
            Object.keys(upRows).forEach(k => delete upRows[k]);
            uploadMeta = [];
        }

        function clearProcRows() {
            const c = document.getElementById('processingFiles');
            if (c) c.innerHTML = '';
            Object.keys(procRows).forEach(k => delete procRows[k]);
        }


        async function checkProcessingStatus() {
            if (phase === 'uploading') return; 
            try {
                const res = await fetch('/media-processing-status/' + encodeURIComponent(PROJECT));
                if (!res.ok) return;           
                renderProcessing(await res.json());
            } catch (e) {
                console.error('Error checking processing status:', e);
            }
        }

        function renderProcessing(data) {
            if (phase === 'uploading') return;
            const box = document.getElementById('processingStatus');
            const files = data.files || [];

            if (phase === 'awaiting') {
                if (data.active) {   
                    phase = 'processing'; clearUploadRows();
                } else if (files.length > 0) {         
                    phase = 'processing'; clearUploadRows();
                } else {  
                    if (Date.now() - awaitingSince > 6000) location.reload();
                    return;
                }
            }

            if (phase === 'processing') {
                box.style.display = 'block';
                updateHeaderProcessing(data);
                updateProcRows(files);
                if (!data.active) { 
                    phase = 'idle';
                    if (pollInterval) clearInterval(pollInterval);
                    setTimeout(() => location.reload(), 1800);
                }
                return;
            }

            if (data.active) {
                phase = 'processing';
                box.style.display = 'block';
                updateHeaderProcessing(data);
                updateProcRows(files);
                updateCancelButton();
            } else {
                box.style.display = 'none';
            }
        }

        function updateProcRows(files) {
            const container = document.getElementById('processingFiles');
            const seen = {};
            files.forEach(f => {
                seen[f.fileName] = true;
                let refs = procRows[f.fileName];
                if (!refs) { refs = createRow(f.fileName); container.appendChild(refs.row); procRows[f.fileName] = refs; }
                refs.stage.textContent = STAGE_LABELS[f.stage] || f.stage.toLowerCase();
                refs.fill.style.width = f.percent + '%';
                refs.pct.textContent = f.percent + '%';
                refs.row.className = 'pf-row pf-' + f.stage.toLowerCase();
                refs.stage.title = f.error || '';
            });
            Object.keys(procRows).forEach(name => {
                if (!seen[name]) { procRows[name].row.remove(); delete procRows[name]; }
            });
        }

        function updateHeaderProcessing(data) {
            const files = data.files || [];
            const total = files.length;
            const done = files.filter(f => f.stage === 'DONE').length;
            const text = document.getElementById('processingText');
            const overall = document.getElementById('processingOverall');
            if (data.active) text.textContent = 'Processing ' + done + ' of ' + total + ' files';
            else if (files.some(f => f.stage === 'CANCELLED')) text.textContent = 'Cancelled';
            else if (files.some(f => f.stage === 'FAILED')) text.textContent = 'Finished with errors';
            else text.textContent = 'Done — ' + total + ' files';
            overall.textContent = (data.overallPercent || 0) + '%';
        }


        async function handleFileUpload(event) {
            const selected = Array.from(event.target.files);
            if (selected.length === 0) return;

            const files = selected.filter(f => ALLOWED.includes((f.name.split('.').pop() || '').toLowerCase()));
            if (files.length === 0) { alert('No supported files selected.'); event.target.value = ''; return; }

            const box = document.getElementById('processingStatus');
            const upContainer = document.getElementById('uploadFiles');

            phase = 'uploading';
            clearUploadRows();
            clearProcRows();
            box.style.display = 'block';

            const formData = new FormData();
            formData.append('folderName', PROJECT);
            let cumulative = 0;
            files.forEach(f => {
                formData.append('files', f);
                const refs = createRow(f.name);
                refs.stage.textContent = 'uploading';
                refs.row.className = 'pf-row pf-uploading';
                upContainer.appendChild(refs.row);
                upRows[f.name] = refs;
                uploadMeta.push({ key: f.name, size: f.size, start: cumulative });
                cumulative += f.size;
            });
            updateHeaderUpload(0);
            updateCancelButton();

            const xhr = new XMLHttpRequest();
            activeXhr = xhr;
            const abort = () => xhr.abort();
            window.addEventListener('beforeunload', abort);

            xhr.upload.addEventListener('progress', function (e) {
                if (!e.lengthComputable) return;
                updateHeaderUpload(Math.round((e.loaded / e.total) * 100));
                uploadMeta.forEach(m => { 
                    const local = Math.max(0, Math.min(1, (e.loaded - m.start) / (m.size || 1)));
                    const refs = upRows[m.key];
                    if (!refs) return;
                    const pct = Math.round(local * 100);
                    refs.fill.style.width = pct + '%';
                    refs.pct.textContent = pct + '%';
                    if (pct >= 100) refs.stage.textContent = 'uploaded ✓';
                });
            });

            xhr.onload = function () {
                window.removeEventListener('beforeunload', abort);
                activeXhr = null;
                event.target.value = '';
                if (xhr.status !== 200) return failUpload('server error');
                let ok = false, msg = '';
                try { const r = JSON.parse(xhr.responseText); ok = (r.success === 'true'); msg = r.error || r.message || ''; }
                catch (e) { return failUpload('invalid response'); }
                Object.values(upRows).forEach(refs => {
                    refs.fill.style.width = '100%'; refs.pct.textContent = '100%';
                    refs.stage.textContent = 'uploaded ✓'; refs.row.className = 'pf-row pf-done';
                });
                if (ok) enterAwaiting(); else failUpload(msg || 'unknown error');
            };
            xhr.onerror = function () {
                window.removeEventListener('beforeunload', abort);
                activeXhr = null; failUpload('network error');
            };
            xhr.onabort = function () {
                window.removeEventListener('beforeunload', abort);
                activeXhr = null; event.target.value = '';
                enterAwaiting();
            };

            xhr.open('POST', '/upload-media');
            xhr.send(formData);
        }

        function updateHeaderUpload(pct) {
            document.getElementById('processingText').textContent =
                'Uploading ' + uploadMeta.length + ' file(s)…';
            document.getElementById('processingOverall').textContent = pct + '%';
        }

        function failUpload(msg) {
            phase = 'idle';
            document.getElementById('processingText').textContent = 'Upload failed: ' + msg;
            updateCancelButton();
            setTimeout(() => {
                document.getElementById('processingStatus').style.display = 'none';
                clearUploadRows();
            }, 3000);
        }

        function enterAwaiting() {
            phase = 'awaiting';
            awaitingSince = Date.now();
            document.getElementById('processingText').textContent = 'Starting…';
            updateCancelButton();
            checkProcessingStatus();                       // poll immediately
            if (!pollInterval) pollInterval = setInterval(checkProcessingStatus, 2000);
        }

        function onCancelClick() {
            if (phase === 'uploading') { if (activeXhr) activeXhr.abort(); }
            else if (phase === 'processing') { cancelProcessing(PROJECT); }
        }

        function updateCancelButton() {
            const btn = document.getElementById('cancelBtn');
            if (!btn) return;
            if (phase === 'uploading') { btn.style.display = ''; btn.disabled = false; btn.textContent = 'Cancel upload'; }
            else if (phase === 'processing') { btn.style.display = ''; btn.disabled = false; btn.textContent = 'Cancel'; }
            else if (phase === 'awaiting') { btn.style.display = ''; btn.disabled = true; btn.textContent = 'Starting…'; }
            else { btn.style.display = 'none'; btn.disabled = true; }
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