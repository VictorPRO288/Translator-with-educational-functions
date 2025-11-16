
function swapLangsFucntion(){
    let src = document.getElementById("inputSourceLang")
    let dest = document.getElementById("inputTargetLang")
    let swap = src.value
    src.value = dest.value
    dest.value = swap

}


document.addEventListener("DOMContentLoaded", async () => {
    const perevod = document.getElementById('addTranslationBtn');
    perevod.onclick = async function(){
        const text = document.getElementById('inputFieldTranslation').value;
        const sourceLang = document.getElementById('inputSourceLang').value;
        const targetLang = document.getElementById('inputTargetLang').value;
        try {
            const response_post = await axios.post('/api/translations',
                {
                    text: text,
                    sourceLang: sourceLang,
                    targetLang: targetLang
                }
            )
            document.getElementById('outputFieldTranslation').value = response_post.data.translatedText;
            // обновим список переводов вверху без перезагрузки
            await refreshTranslationsList();
        } catch (e) {
            alert('Ошибка перевода. Попробуйте снова.');
        }
    }

    const analyzeBtn = document.getElementById('analyzeBtn');
    if (analyzeBtn) {
        analyzeBtn.onclick = async function(){
            const sourceText = document.getElementById('inputFieldTranslation').value;
            const translatedText = document.getElementById('outputFieldTranslation').value;
            const sourceLanguage = document.getElementById('inputSourceLang').value;
            const targetLanguage = document.getElementById('inputTargetLang').value;
            const list = document.getElementById('hintsList');
            const transcription = document.getElementById('transcription');
            list.innerHTML = '';
            if (transcription) transcription.innerHTML = '';
            try {
                const resp = await axios.post('/hints/analyze', {
                    sourceLanguage, targetLanguage, sourceText, translatedText
                }, { headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }});
                let data = resp.data;
                if (typeof data === 'string') {
                    try { data = JSON.parse(data); } catch(e) { data = { suggestions: [] }; }
                }
                const suggestions = Array.isArray(data?.suggestions) ? data.suggestions : [];
                if (!suggestions.length) {
                    const li = document.createElement('li');
                    li.className = 'hint-card';
                    li.textContent = 'Подсказок не найдено';
                    list.appendChild(li);
                    return;
                }
                suggestions.forEach(s => {
                    const li = document.createElement('li');
                    li.className = 'hint-card';
                    li.innerHTML = `<strong>${s.type || 'rule'}</strong>: ${s.message || ''}${s.replacement ? ` — <em>${s.replacement}</em>` : ''}`;
                    list.appendChild(li);
                })
                // request IPA transcription from Gemini service
                try {
                    const ipaResp = await axios.post('/hints/transcribe', {
                        text: translatedText,
                        languageCode: targetLanguage
                    }, { headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }});
                    if (ipaResp?.data?.ipa && transcription) {
                        transcription.textContent = `IPA: ${ipaResp.data.ipa}`;
                    }
                } catch(_) {}
            } catch (err) {
                const li = document.createElement('li');
                li.className = 'hint-card';
                li.textContent = 'Ошибка анализа. Попробуйте снова.';
                list.appendChild(li);
            }
        }
    }

    const speakBtn = document.getElementById('speakBtn');
    if (speakBtn) {
        // ensure voices are loaded and pick best matching voice by language
        function loadVoices() {
            return new Promise(resolve => {
                const voices = window.speechSynthesis.getVoices();
                if (voices && voices.length) return resolve(voices);
                window.speechSynthesis.onvoiceschanged = () => resolve(window.speechSynthesis.getVoices());
            });
        }

        function pickVoice(voices, langCode) {
            if (!Array.isArray(voices)) return null;
            const lc = (langCode || '').toLowerCase();
            let voice = voices.find(v => (v.lang || '').toLowerCase() === lc);
            if (voice) return voice;
            voice = voices.find(v => (v.lang || '').toLowerCase().startsWith(lc.split('-')[0] + '-'));
            if (voice) return voice;
            voice = voices.find(v => (v.lang || '').toLowerCase().startsWith(lc.split('-')[0]));
            return voice || voices[0] || null;
        }

        speakBtn.onclick = async function(){
            const text = document.getElementById('outputFieldTranslation').value || '';
            if (!text.trim()) { return; }
            const targetLang = document.getElementById('inputTargetLang').value || 'ru-RU';
            try {
                const resp = await axios.get(`/hints/tts`, { params: { text, lang: targetLang } , responseType: 'blob' });
                const url = URL.createObjectURL(resp.data);
                const audio = new Audio(url);
                audio.play();
                return;
            } catch (e) {
                // fallback to browser TTS
            }
            if (!('speechSynthesis' in window)) { alert('Ваш браузер не поддерживает озвучивание.'); return; }
            const voices = await loadVoices();
            const lang = (document.getElementById('inputTargetLang').value || 'ru').toLowerCase();
            const utterance = new SpeechSynthesisUtterance(text);
            const voice = pickVoice(voices, lang);
            if (voice) utterance.voice = voice;
            utterance.lang = voice?.lang || lang;
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            window.speechSynthesis.cancel();
            window.speechSynthesis.speak(utterance);
        }
    }
    await refreshTranslationsList();

    // Delegate clicks for inline actions so we always have correct text and avoid empty payloads
    document.getElementById('translationsList').addEventListener('click', async (e) => {
        const btn = e.target.closest('button');
        if (!btn) return;
        const li = e.target.closest('li.translation-card');
        if (!li) return;
        const id = btn.getAttribute('data-id') || li.getAttribute('data-id');
        const textEl = li.querySelector('.translated');
        const translatedText = textEl ? textEl.textContent : '';
        if (btn.classList.contains('analyze-inline')) {
            const sourceLanguage = btn.getAttribute('data-source-lang');
            const targetLanguage = btn.getAttribute('data-target-lang');
            const hintsEl = document.getElementById(`hints-${id}`);
            const ipaEl = document.getElementById(`transcription-${id}`);
            if (hintsEl) hintsEl.innerHTML = '';
            if (ipaEl) ipaEl.innerHTML = '';
            try {
                const resp = await axios.post('/hints/analyze', { sourceLanguage, targetLanguage, sourceText: '', translatedText }, { headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }});
                let data = resp.data;
                if (typeof data === 'string') { try { data = JSON.parse(data); } catch(_) { data = { suggestions: [] }; } }
                const suggestions = Array.isArray(data?.suggestions) ? data.suggestions : [];
                if (suggestions.length) {
                    suggestions.forEach(s => {
                        const liHint = document.createElement('li');
                        liHint.className = 'hint-card';
                        liHint.innerHTML = `<strong>${s.type || 'rule'}</strong>: ${s.message || ''}${s.replacement ? ` — <em>${s.replacement}</em>` : ''}`;
                        hintsEl.appendChild(liHint);
                    })
                } else {
                    // если пусто — ничего не показываем, чтобы не мешать UI
                }
                try {
                    const ipaResp = await axios.post('/hints/transcribe', { text: translatedText, languageCode: targetLanguage }, { headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }});
                    if (ipaResp?.data?.ipa && ipaEl) { ipaEl.textContent = `IPA: ${ipaResp.data.ipa}`; }
                    if (Array.isArray(ipaResp?.data?.examples) && ipaResp.data.examples.length) {
                        openModal(translatedText, ipaResp.data.ipa, ipaResp.data.examples, li);
                    }
                } catch(_) {}
            } catch(_) {
                const liHint = document.createElement('li');
                liHint.className = 'hint-card';
                liHint.textContent = 'Ошибка анализа';
                if (hintsEl) hintsEl.appendChild(liHint);
            }
        }
        if (btn.classList.contains('speak-inline')) {
            const targetLang = btn.getAttribute('data-target-lang') || 'ru-RU';
            try {
                const resp = await axios.get('/hints/tts', { params: { text: translatedText, lang: targetLang }, responseType: 'blob' });
                const url = URL.createObjectURL(resp.data);
                const audio = new Audio(url);
                audio.play();
            } catch(_) {
                if (!('speechSynthesis' in window)) return;
                const utterance = new SpeechSynthesisUtterance(translatedText);
                utterance.lang = targetLang;
                window.speechSynthesis.cancel();
                window.speechSynthesis.speak(utterance);
            }
        }
        if (btn.classList.contains('quiz-inline')) {
            const originalText = btn.getAttribute('data-original-text');
            const translatedText = btn.getAttribute('data-translated-text');
            const sourceLang = btn.getAttribute('data-source-lang');
            const targetLang = btn.getAttribute('data-target-lang');
            
            try {
                const resp = await axios.post('/api/quiz/generate', {
                    originalText: originalText,
                    translatedText: translatedText,
                    sourceLang: sourceLang,
                    targetLang: targetLang
                });
                
                if (resp.data && resp.data.id) {
                    // Redirect to quiz page
                    window.location.href = `/quiz.html?id=${resp.data.id}`;
                }
            } catch (error) {
                alert('Ошибка при создании теста: ' + (error.response?.data?.message || error.message));
            }
        }
    });
});
async function refreshTranslationsList(){
    const ul = document.getElementById('translationsList');
    if (!ul) return;
    try {
        const list = await axios.get('/api/translations');
        const reverse = list.data.slice().reverse();
        ul.innerHTML = '';
        for (let i = 0; i < reverse.length; i++) {
            const translation = reverse[i];
            const translationElement = `
                <li class="translation-card" data-id="${translation.id}">
                    <div class="translation-content">
                        <div class="language-pair">
                            <span >${translation.sourceLang}</span>
                            <span class="arrow-icon">→</span>
                            <span >${translation.targetLang}</span>
                        </div>
                        <p class="translation-text"><strong>${translation.originalText}</strong> <span class="arrow-icon">→</span> <span class="translated">${translation.translatedText}</span></p>
                        <div class="actions-inline">
                            <button class="analyze-inline" data-id="${translation.id}" data-source-lang="${translation.sourceLang}" data-target-lang="${translation.targetLang}">Подсказки и примеры</button>
                            <button class="speak-inline" data-id="${translation.id}" data-target-lang="${translation.targetLang}">► Озвучить</button>
                            <button class="quiz-inline" data-id="${translation.id}" data-original-text="${translation.originalText}" data-translated-text="${translation.translatedText}" data-source-lang="${translation.sourceLang}" data-target-lang="${translation.targetLang}">🧠 Проверить знания</button>
                            <button class="delTranslations" onclick="removeTranslation(${translation.id})">Удалить</button>
                        </div>
                        <div class="transcription" id="transcription-${translation.id}"></div>
                        <ul class="hints" id="hints-${translation.id}"></ul>
                    </div>
                </li>
            `;
            ul.innerHTML += translationElement;
        }
    } catch(_) {}
}

async function removeTranslation(id){
    console.log(id)
    const del = await axios.delete(`/api/translations/${id}`)
    setTimeout(() => {
        window.location.reload()
    }, 100)
    
}

// Modal helpers
function openModal(text, ipa, examples, anchorLi){
    const modal = document.getElementById('modal');
    const body = document.getElementById('modal-body');
    const title = document.getElementById('modal-title');
    if (!modal || !body) return;
    title.textContent = 'Транскрипция и примеры';
    const list = (examples || []).map(e => `<li>${e}</li>`).join('');
    body.innerHTML = `
        <div style="margin-bottom:8px;"><strong>Текст:</strong> ${escapeHtml(text)}</div>
        <div style="margin-bottom:8px;"><strong>IPA:</strong> ${ipa ? `<code>${escapeHtml(ipa)}</code>` : '—'}</div>
        <div><strong>Примеры:</strong><ul style="margin:6px 0 0 18px;">${list}</ul></div>
    `;
    // position over the card (anchor)
    modal.style.display = 'flex';
    try {
        const rect = anchorLi.getBoundingClientRect();
        const vwTop = rect.top + window.scrollY - 12; // small offset
        const vwLeft = rect.left + window.scrollX + rect.width/2;
        const content = modal.querySelector('.modal-content');
        if (content) {
            content.style.position = 'absolute';
            content.style.top = `${vwTop}px`;
            content.style.left = `${Math.max(16, vwLeft - content.offsetWidth/2)}px`;
            content.style.maxWidth = '520px';
        }
    } catch(_) {}
}

function closeModal(){
    const modal = document.getElementById('modal');
    if (modal) modal.style.display = 'none';
}

document.addEventListener('click', (e) => {
    const target = e.target;
    if (target && target.id === 'modal-close') { closeModal(); }
    if (target && target.id === 'modal') { closeModal(); }
});

function escapeHtml(s){
    return (s || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c]));
}
