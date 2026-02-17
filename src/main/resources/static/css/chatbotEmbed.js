/**
 * Harmony Chatbot — Embeddable Widget
 *
 * Usage (add to any website):
 *   <script src="https://your-app.onrender.com/chatbot-embed.js" defer><\/script>
 *
 * Optional config (set BEFORE the script tag):
 *   <script>window.HarmonyChatConfig = { serverUrl: 'https://your-app.onrender.com' };<\/script>
 */
(function () {
    const SERVER = (window.HarmonyChatConfig && window.HarmonyChatConfig.serverUrl)
        ? window.HarmonyChatConfig.serverUrl.replace(/\/$/, '')
        : '';  // empty = same origin (for index.html served by Spring Boot)

    const userLang = (navigator.language || 'en').split('-')[0].toLowerCase();
    const i18n = {
        en: { welcome:'Welcome!', sub:'Enter your details to start chatting.', namePh:'Your name', emailPh:'Your email', start:'Start Chat', skip:'Skip', greet:'Hi! How can I help you today?', thinking:'Thinking…', serverErr:'Server error.', inputPh:'Ask a question…', bookBtn:'Book Appointment', send:'Send' },
        es: { welcome:'¡Bienvenido!', sub:'Ingresa tus datos para comenzar.', namePh:'Tu nombre', emailPh:'Tu correo', start:'Iniciar chat', skip:'Omitir', greet:'¡Hola! ¿En qué puedo ayudarte?', thinking:'Pensando…', serverErr:'Error del servidor.', inputPh:'Haz una pregunta…', bookBtn:'Reservar cita', send:'Enviar' },
        fr: { welcome:'Bienvenue!', sub:'Entrez vos coordonnées pour commencer.', namePh:'Votre nom', emailPh:'Votre email', start:'Commencer', skip:'Passer', greet:'Bonjour! Comment puis-je vous aider?', thinking:'Réflexion…', serverErr:'Erreur serveur.', inputPh:'Posez une question…', bookBtn:'Prendre rendez-vous', send:'Envoyer' },
        de: { welcome:'Willkommen!', sub:'Geben Sie Ihre Daten ein.', namePh:'Ihr Name', emailPh:'Ihre E-Mail', start:'Chat starten', skip:'Überspringen', greet:'Hallo! Wie kann ich Ihnen helfen?', thinking:'Denke nach…', serverErr:'Serverfehler.', inputPh:'Stellen Sie eine Frage…', bookBtn:'Termin buchen', send:'Senden' },
        pt: { welcome:'Bem-vindo!', sub:'Insira seus dados para começar.', namePh:'Seu nome', emailPh:'Seu email', start:'Iniciar chat', skip:'Pular', greet:'Olá! Como posso ajudar?', thinking:'Pensando…', serverErr:'Erro no servidor.', inputPh:'Faça uma pergunta…', bookBtn:'Agendar consulta', send:'Enviar' },
        zh: { welcome:'欢迎!', sub:'请输入您的信息开始聊天。', namePh:'您的姓名', emailPh:'您的邮箱', start:'开始聊天', skip:'跳过', greet:'您好！有什么可以帮助您？', thinking:'思考中…', serverErr:'服务器错误。', inputPh:'请提问…', bookBtn:'预约', send:'发送' },
    };
    const t = i18n[userLang] || i18n.en;

    const BOOKING_KEYWORDS = ['book','appointment','schedule','reserve','rendez','reservar','buchen','agendar','预约','حجز'];

    // ── Inject styles ──────────────────────────────────────────────────────────

    const style = document.createElement('style');
    style.textContent = `
        #hc-bubble{position:fixed;bottom:24px;right:24px;width:60px;height:60px;background:#0d6efd;color:white;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:26px;cursor:pointer;box-shadow:0 10px 30px rgba(0,0,0,.3);z-index:2147483647;overflow:hidden;visibility:hidden;}
        #hc-bubble img{width:60px;height:60px;border-radius:50%;object-fit:cover;display:none;}
        #hc-win{position:fixed;bottom:100px;right:24px;width:360px;height:560px;background:#fff;border-radius:16px;box-shadow:0 20px 40px rgba(0,0,0,.25);display:none;flex-direction:column;z-index:2147483647;overflow:hidden;font-family:Arial,sans-serif;font-size:14px;}
        #hc-banner{display:none;width:100%;height:120px;object-fit:cover;object-position:center top;flex-shrink:0;}
        #hc-header{padding:10px 14px;background:#0d6efd;color:white;display:flex;justify-content:space-between;align-items:center;flex-shrink:0;}
        #hc-header img{width:32px;height:32px;border-radius:50%;object-fit:cover;margin-right:8px;display:none;border:2px solid rgba(255,255,255,.6);}
        #hc-chips{display:flex;gap:8px;padding:10px;flex-wrap:wrap;border-bottom:1px solid #eee;flex-shrink:0;}
        #hc-chips:empty{display:none;}
        .hc-chip{padding:6px 12px;border-radius:16px;border:1px solid #ccc;background:#f0f0f0;font-size:12px;cursor:pointer;}
        .hc-chip:hover{background:#e0e0e0;}
        #hc-lead{padding:20px;display:flex;flex-direction:column;gap:12px;flex:1;justify-content:center;}
        #hc-lead h4{margin:0;font-size:15px;}
        #hc-lead p{margin:0;font-size:13px;color:#666;}
        #hc-lead input{padding:9px 12px;border-radius:8px;border:1px solid #ccc;font-size:13px;outline:none;}
        .hc-btn-primary{background:#0d6efd;color:white;border:none;border-radius:8px;padding:10px;cursor:pointer;font-size:14px;}
        .hc-btn-skip{background:none;border:none;color:#999;font-size:12px;cursor:pointer;text-decoration:underline;align-self:center;}
        #hc-msgs{flex:1;padding:12px;overflow-y:auto;}
        .hc-msg{margin-bottom:10px;max-width:80%;padding:8px 12px;border-radius:14px;line-height:1.4;font-size:13px;}
        .hc-user{background:#0d6efd;color:white;margin-left:auto;border-bottom-right-radius:4px;}
        .hc-bot{background:#eee;color:#333;margin-right:auto;border-bottom-left-radius:4px;}
        .hc-typing span{display:inline-block;width:7px;height:7px;background:#999;border-radius:50%;margin:0 2px;animation:hc-blink 1.2s infinite;}
        .hc-typing span:nth-child(2){animation-delay:.2s;}.hc-typing span:nth-child(3){animation-delay:.4s;}
        @keyframes hc-blink{0%,80%,100%{opacity:0;}40%{opacity:1;}}
        .hc-rating-row{display:flex;gap:8px;margin-bottom:6px;}
        .hc-rbtn{background:none;border:1px solid #ddd;border-radius:20px;padding:3px 10px;font-size:12px;cursor:pointer;color:#666;}
        .hc-rbtn:hover{background:#f0f0f0;}
        .hc-rbtn.up{background:#d4edda;border-color:#28a745;color:#28a745;}.hc-rbtn.dn{background:#f8d7da;border-color:#dc3545;color:#dc3545;}
        .hc-book{display:inline-block;margin-top:8px;padding:8px 16px;background:#0d6efd;color:white;border-radius:20px;text-decoration:none;font-size:13px;font-weight:bold;}
        #hc-input-row{display:flex;padding:10px;gap:8px;border-top:1px solid #eee;flex-shrink:0;}
        #hc-input-row textarea{flex:1;resize:none;padding:8px;border-radius:8px;border:1px solid #ccc;font-size:13px;}
        #hc-input-row button{background:#0d6efd;color:white;border:none;border-radius:20px;padding:8px 14px;cursor:pointer;}
        #hc-chatui{display:none;flex-direction:column;flex:1;overflow:hidden;}
    `;
    document.head.appendChild(style);

    // ── Inject HTML ────────────────────────────────────────────────────────────

    document.body.insertAdjacentHTML('beforeend', `
        <div id="hc-bubble"><img id="hc-bavatar" src="" alt=""/><span id="hc-bemoji">💬</span></div>
        <div id="hc-win">
            <img id="hc-banner" src="" alt=""/>
            <div id="hc-header">
                <div style="display:flex;align-items:center;"><img id="hc-havatar" src="" alt=""/><span>Assistant</span></div>
                <span id="hc-close" style="cursor:pointer;">✕</span>
            </div>
            <div id="hc-lead">
                <h4>${t.welcome}</h4><p>${t.sub}</p>
                <input type="text"  id="hc-name"  placeholder="${t.namePh}">
                <input type="email" id="hc-email" placeholder="${t.emailPh}">
                <button class="hc-btn-primary" id="hc-lead-submit">${t.start}</button>
                <button class="hc-btn-skip"    id="hc-lead-skip">${t.skip}</button>
            </div>
            <div id="hc-chatui">
                <div id="hc-chips"></div>
                <div id="hc-msgs"><div class="hc-msg hc-bot">${t.greet}</div></div>
                <div id="hc-input-row">
                    <textarea id="hc-input" rows="2" placeholder="${t.inputPh}"></textarea>
                    <button id="hc-send">${t.send}</button>
                </div>
            </div>
        </div>
    `);

    // ── Wire up ────────────────────────────────────────────────────────────────

    const bubble = document.getElementById('hc-bubble');
    const win    = document.getElementById('hc-win');
    const msgs   = document.getElementById('hc-msgs');
    const input  = document.getElementById('hc-input');
    let bookingUrl = '';

    bubble.onclick = () => { win.style.display='flex'; bubble.style.display='none'; };
    document.getElementById('hc-close').onclick = () => { win.style.display='none'; bubble.style.display='flex'; };

    function openChat() {
        document.getElementById('hc-lead').style.display='none';
        document.getElementById('hc-chatui').style.display='flex';
    }

    document.getElementById('hc-lead-submit').onclick = async () => {
        const name  = document.getElementById('hc-name').value.trim();
        const email = document.getElementById('hc-email').value.trim();
        if (name || email) {
            try { await fetch(SERVER+'/api/leads', { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({name,email}) }); } catch(e){}
        }
        openChat();
    };
    document.getElementById('hc-lead-skip').onclick = () => openChat();

    function addMsg(text, cls) {
        const d = document.createElement('div');
        d.className = `hc-msg ${cls}`;
        d.textContent = text;
        msgs.appendChild(d); msgs.scrollTop = msgs.scrollHeight;
        return d;
    }
    function addTyping() {
        const d = document.createElement('div');
        d.className = 'hc-msg hc-bot hc-typing';
        d.innerHTML = '<span></span><span></span><span></span>';
        msgs.appendChild(d); msgs.scrollTop = msgs.scrollHeight;
        return d;
    }
    function addRating(logId) {
        const row = document.createElement('div'); row.className='hc-rating-row';
        row.innerHTML=`<button class="hc-rbtn" data-id="${logId}" data-v="1">👍</button><button class="hc-rbtn" data-id="${logId}" data-v="-1">👎</button>`;
        row.querySelectorAll('.hc-rbtn').forEach(btn => {
            btn.onclick = async () => {
                const v = parseInt(btn.dataset.v);
                await fetch(SERVER+'/api/analytics/rate',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({id:parseInt(btn.dataset.id),rating:v})});
                row.querySelectorAll('.hc-rbtn').forEach(b=>b.classList.remove('up','dn'));
                btn.classList.add(v===1?'up':'dn');
            };
        });
        msgs.appendChild(row); msgs.scrollTop=msgs.scrollHeight;
    }
    function addBookingBtn() {
        const a = document.createElement('a'); a.href=bookingUrl; a.target='_blank'; a.className='hc-book'; a.textContent=t.bookBtn;
        const w = document.createElement('div'); w.style.paddingLeft='12px'; w.appendChild(a);
        msgs.appendChild(w); msgs.scrollTop=msgs.scrollHeight;
    }

    async function sendMsg(textOverride) {
        const text = (textOverride||input.value).trim(); if(!text) return;
        addMsg(text,'hc-user'); input.value='';
        const prompt = userLang!=='en' ? `[Respond in language: ${userLang}] ${text}` : text;
        const typing = addTyping();
        try {
            const res  = await fetch(SERVER+'/api/chat',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({question:prompt})});
            const data = await res.json();
            typing.remove();
            addMsg(data.answer||'No response.','hc-bot');
            if(data.id) addRating(data.id);
            if(bookingUrl && BOOKING_KEYWORDS.some(k=>text.toLowerCase().includes(k))) addBookingBtn();
        } catch(e){ typing.remove(); addMsg(t.serverErr,'hc-bot'); }
    }

    document.getElementById('hc-send').onclick = ()=>sendMsg();
    input.addEventListener('keydown', e=>{ if(e.key==='Enter'){e.preventDefault();sendMsg();}});

    // ── Load theme ─────────────────────────────────────────────────────────────

    async function loadTheme() {
        try {
            const res   = await fetch(SERVER+'/api/theme?t='+Date.now());
            const theme = await res.json();

            // Colors
            const hColor = theme.headerColor||'#0d6efd';
            const iColor = theme.iconColor||'#0d6efd';
            bubble.style.background = iColor;
            document.getElementById('hc-header').style.background = hColor;
            document.getElementById('hc-win').style.background = theme.backgroundColor||'#fff';
            msgs.style.background = theme.backgroundColor||'#fff';
            msgs.style.color      = theme.textColor||'#000';

            bookingUrl = theme.bookingUrl||'';

            // Update chip colors inline
            const chipStyle = document.createElement('style');
            chipStyle.textContent=`.hc-chip{background:${theme.chipBackgroundColor||'#f0f0f0'};border-color:${theme.chipBorderColor||'#ccc'}}.hc-chip:hover{background:${theme.chipHoverColor||'#e0e0e0'}}.hc-btn-primary,.hc-book,#hc-input-row button,#hc-send{background:${iColor}!important}.hc-user{background:${iColor}!important}#hc-header{background:${hColor}!important}#hc-bubble{background:${iColor}!important}`;
            document.head.appendChild(chipStyle);

            // Avatar
            const bavatar = document.getElementById('hc-bavatar');
            const bemoji  = document.getElementById('hc-bemoji');
            const havatar = document.getElementById('hc-havatar');
            if(theme.avatarData){
                bavatar.src=theme.avatarData;bavatar.style.display='block';bemoji.style.display='none';bubble.style.padding='0';
                havatar.src=theme.avatarData;havatar.style.display='inline-block';
            }

            // Banner
            const banner=document.getElementById('hc-banner');
            if(theme.bannerData){banner.src=theme.bannerData;banner.style.display='block';}

            // Chips
            const chipsEl=document.getElementById('hc-chips');
            let suggestions=[];
            if(theme.suggestionsJson){try{suggestions=JSON.parse(theme.suggestionsJson);}catch(e){}}
            chipsEl.innerHTML='';
            suggestions.forEach(label=>{
                const c=document.createElement('div');c.className='hc-chip';c.textContent=label;
                c.onclick=()=>{sendMsg(label);chipsEl.style.display='none';};
                chipsEl.appendChild(c);
            });

            bubble.style.visibility='visible';
        } catch(e){ bubble.style.visibility='visible'; }
    }

    loadTheme();
})();
