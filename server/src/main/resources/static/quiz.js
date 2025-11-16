let currentQuiz = null;

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const quizId = urlParams.get('id');
    
    if (quizId) {
        loadQuiz(quizId);
    } else {
        showError('ID теста не найден');
    }
});

async function loadQuiz(quizId) {
    try {
        const response = await axios.get(`/api/quiz/${quizId}`);
        currentQuiz = response.data;
        displayQuiz(currentQuiz);
    } catch (error) {
        console.error('Ошибка загрузки теста:', error);
        showError('Ошибка при загрузке теста: ' + (error.response?.data?.message || error.message));
    }
}

function displayQuiz(quiz) {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('quiz-content').style.display = 'block';
    
    document.getElementById('quiz-title').textContent = quiz.title;
    document.getElementById('quiz-info').textContent = 
        `Оригинал: "${quiz.originalText}" → Перевод: "${quiz.translatedText}"`;
    
    const questionsContainer = document.getElementById('questions-container');
    questionsContainer.innerHTML = '';
    
    quiz.questions.forEach((question, index) => {
        const questionCard = document.createElement('div');
        questionCard.className = 'question-card';
        questionCard.innerHTML = `
            <div class="question-text">${index + 1}. ${question.question}</div>
            <div class="options">
                ${question.options.map((option, optionIndex) => `
                    <label class="option">
                        <input type="radio" name="question_${question.id}" value="${option}">
                        <span>${option}</span>
                    </label>
                `).join('')}
            </div>
        `;
        questionsContainer.appendChild(questionCard);
    });
    
    // Add event listeners for option selection
    document.querySelectorAll('.option').forEach(option => {
        option.addEventListener('click', function() {
            const radio = this.querySelector('input[type="radio"]');
            const name = radio.name;
            
            // Remove selected class from all options with same name
            document.querySelectorAll(`input[name="${name}"]`).forEach(r => {
                r.closest('.option').classList.remove('selected');
            });
            
            // Add selected class to clicked option
            radio.checked = true;
            this.classList.add('selected');
        });
    });
}

document.getElementById('quiz-form').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    if (!currentQuiz) return;
    
    const answers = [];
    const formData = new FormData(this);
    
    currentQuiz.questions.forEach(question => {
        const answer = formData.get(`question_${question.id}`);
        if (answer) {
            answers.push({
                questionId: question.id,
                answer: answer
            });
        }
    });
    
    if (answers.length !== currentQuiz.questions.length) {
        alert('Пожалуйста, ответьте на все вопросы');
        return;
    }
    
    try {
        const response = await axios.post('/api/quiz/submit', {
            quizId: currentQuiz.id,
            answers: answers
        });
        
        showResults(response.data);
    } catch (error) {
        console.error('Ошибка отправки теста:', error);
        alert('Ошибка при отправке теста: ' + (error.response?.data?.message || error.message));
    }
});

function showResults(result) {
    // Показать результаты в конце страницы теста, не скрывая вопросы
    const emoji = getEmojiByScore(result.score);
    const gradeText = decorateGrade(result.grade, result.score);

    // Создаём/находим контейнер для инлайн-результата
    let inline = document.getElementById('inline-results');
    if (!inline) {
        inline = document.createElement('div');
        inline.id = 'inline-results';
        inline.className = 'result-container';
        const form = document.getElementById('quiz-form');
        if (form && form.parentNode) {
            form.parentNode.appendChild(inline);
        }
    }

    inline.innerHTML = `
        <h2 class="result-title">Результаты теста</h2>
        <div class="score">${result.score}% ${emoji}</div>
        <div class="grade">${gradeText}</div>
        <div class="result-details">
            <h3>Детали результатов:</h3>
            <p>Правильных ответов: ${result.correctAnswers} из ${result.totalQuestions}</p>
            <div style="margin-top: 20px;">
                ${result.results.map((q, index) => renderQuestionResult(q, index)).join('')}
            </div>
        </div>
    `;

    // Заблокируем форму, чтобы нельзя было менять ответы после отправки
    const inputs = document.querySelectorAll('#quiz-form input[type="radio"]');
    inputs.forEach(i => i.disabled = true);

    // Подсветим вопросы непосредственно в форме
    try {
        (result.results || []).forEach(r => {
            const name = `question_${r.questionId}`;
            const radios = document.querySelectorAll(`input[name="${name}"]`);
            if (!radios || !radios.length) return;
            const card = radios[0].closest('.question-card');
            if (!card) return;
            // сравним выбранное значение с правильным без учета регистра и лишних пробелов
            let selectedValue = '';
            radios.forEach(rd => { if (rd.checked) selectedValue = (rd.value || ''); });
            const sameRaw = compareAnswers(selectedValue, r.correctAnswer || '');
            if (sameRaw) { card.classList.remove('incorrect'); card.classList.add('correct'); }
            else { card.classList.remove('correct'); card.classList.add('incorrect'); }
            // Подсветим выбранный вариант
            let selectedLabel = null;
            radios.forEach(rd => {
                const label = rd.closest('.option');
                if (!label) return;
                label.classList.remove('selected-correct','selected-incorrect');
                if (rd.checked) selectedLabel = label;
            });
            if (selectedLabel) { selectedLabel.classList.add(sameRaw ? 'selected-correct' : 'selected-incorrect'); }
        });
    } catch (_) {}

    // Прокрутим к результатам
    inline.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function renderQuestionResult(q, index){
    const yourRaw = (q.userAnswer || '').trim();
    const correctRaw = (q.correctAnswer || '').trim();
    const sameRaw = compareAnswers(yourRaw, correctRaw);
    const icon = sameRaw ? '✅' : '❌';
    const yourAns = escapeHtml(yourRaw || '—');
    const correctAns = escapeHtml(correctRaw || '—');
    const yourSpanClass = sameRaw ? 'right' : 'wrong';
    return `
        <div class="result-question ${sameRaw ? 'correct' : 'incorrect'}">
            <div style="margin-bottom:6px;"><strong>Вопрос ${index + 1}:</strong> ${escapeHtml(q.question || '')} ${icon}</div>
            <div><strong>Ваш ответ:</strong> <span class="ans ${yourSpanClass}">${yourAns}${sameRaw ? ' ✓' : ' ✗'}</span></div>
            <div><strong>Правильный ответ:</strong> <span class="ans right">${correctAns} ✓</span></div>
        </div>
    `;
}

function compareAnswers(a, b){
    const norm = (s) => (s || '').trim().replace(/\s+/g,' ').toLowerCase();
    return norm(a) === norm(b);
}

function getEmojiByScore(score){
    if (score >= 90) return '🌟';
    if (score >= 80) return '🎉';
    if (score >= 70) return '👍';
    if (score >= 60) return '🙂';
    if (score >= 40) return '😕';
    return '😞';
}

function decorateGrade(grade, score){
    const map = {
        'Отлично': 'Отлично',
        'Хорошо': 'Хорошо',
        'Удовлетворительно': 'Удовлетворительно',
        'Зачет': 'Зачет',
        'Незачет': 'Незачет'
    };
    const base = map[grade] || grade || '';
    return base;
}

// Локальная функция экранирования HTML для безопасности вывода
function escapeHtml(s){
    return (s || '').replace(/[&<>"']/g, function(c){
        return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c]) || c;
    });
}

function showError(message) {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('error-content').style.display = 'block';
    document.getElementById('error-message').textContent = message;
}

function goBack() {
    window.location.href = '/';
}
