// =========================================================================
// 1. BẢNG MÀU THẦN LONG THEO CẢNH GIỚI (Level 4 -> 8)
// =========================================================================
const DRAGON_THEMES = {
    4: { // Nguyên Anh: Thanh Long
        core: '#059669', mid: '#10b981', light: '#6ee7b7',
        belly: '#d1fae5', glow: '#34d399', mane: '#a7f3d0', eye: '#ffffff'
    },
    5: { // Hóa Thần: Hỏa Long
        core: '#991b1b', mid: '#ef4444', light: '#fb923c',
        belly: '#fef08a', glow: '#f97316', mane: '#fde047', eye: '#ffffff'
    },
    6: { // Luyện Hư: Tử Long
        core: '#581c87', mid: '#9333ea', light: '#d8b4fe',
        belly: '#f3e8ff', glow: '#c084fc', mane: '#e9d5ff', eye: '#fde047'
    },
    7: { // Đại Thừa: Viêm Long
        core: '#b45309', mid: '#f59e0b', light: '#fde047',
        belly: '#fffbeb', glow: '#fb7185', mane: '#fef08a', eye: '#ffffff'
    },
    8: { // Độ Kiếp Phi Thăng: Kim Long Chí Tôn
        core: '#92400e', mid: '#f59e0b', light: '#fef08a',
        belly: '#ffffff', glow: '#fbbf24', mane: '#fef9c3', eye: '#ef4444'
    }
};

function getDragonTheme(level) {
    return DRAGON_THEMES[level] || DRAGON_THEMES[8];
}

// =========================================================================
// 2. HÀM VẼ THẦN LONG PHƯƠNG ĐÔNG (THÂN LIỀN KHỐI, VẢY, SỪNG HƯƠU, 4 CHÂN)
// =========================================================================
function drawEasternDragon(ctx, segments, theme, scale, time) {
    const n = segments.length;
    if (n < 6) return;

    // Tính góc hướng & bán kính mượt cho từng đốt sống
    const angles = new Array(n);
    const radii = new Array(n);
    const leftPts = new Array(n);
    const rightPts = new Array(n);

    for (let i = 0; i < n; i++) {
        const prev = segments[Math.max(0, i - 1)];
        const next = segments[Math.min(n - 1, i + 1)];
        angles[i] = Math.atan2(prev.y - next.y, prev.x - next.x);

        const t = i / (n - 1); // 0 ở đầu, 1 ở đuôi
        // Dáng thân: cổ hơi thon, ngực phình nhẹ rồi thuôn dần về đuôi
        let profile;
        if (t < 0.12) {
            profile = 0.72 + (t / 0.12) * 0.28;
        } else {
            profile = Math.pow(1 - (t - 0.12) / 0.88, 0.85);
        }
        radii[i] = Math.max(1.2 * scale, 13.5 * scale * profile);

        const perp = angles[i] + Math.PI / 2;
        leftPts[i] = {
            x: segments[i].x + Math.cos(perp) * radii[i],
            y: segments[i].y + Math.sin(perp) * radii[i]
        };
        rightPts[i] = {
            x: segments[i].x - Math.cos(perp) * radii[i],
            y: segments[i].y - Math.sin(perp) * radii[i]
        };
    }

    ctx.save();
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    // --- A. CHÙM LÔNG ĐUÔI RỰC LỬA ---
    const tail = segments[n - 1];
    const tailAngle = angles[n - 1] + Math.PI; // Hướng ngược ra sau đuôi
    ctx.save();
    ctx.translate(tail.x, tail.y);
    ctx.rotate(tailAngle);
    ctx.shadowColor = theme.glow;
    ctx.shadowBlur = 18 * scale;
    for (let f = -2; f <= 2; f++) {
        const len = (24 - Math.abs(f) * 4) * scale;
        const sway = Math.sin(time * 0.012 + f) * 5 * scale;
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(len * 0.5, f * 5 * scale + sway, len, f * 3 * scale - sway * 0.6);
        ctx.strokeStyle = f === 0 ? theme.belly : theme.light;
        ctx.lineWidth = (3.2 - Math.abs(f) * 0.5) * scale;
        ctx.globalAlpha = 0.85;
        ctx.stroke();
    }
    ctx.restore();

    // --- B. BỐN CHÂN RỒNG & MÓNG VUỐT (VẼ DƯỚI THÂN) ---
    const legIndices = [Math.floor(n * 0.24), Math.floor(n * 0.62)];
    legIndices.forEach((idx, legOrder) => {
        if (idx <= 1 || idx >= n - 2) return;
        const base = segments[idx];
        const ang = angles[idx];
        const legScale = scale * (legOrder === 0 ? 1.0 : 0.82);

        [-1, 1].forEach(side => {
            const swing = Math.sin(time * 0.008 + legOrder * 2 + side) * 0.22;
            ctx.save();
            ctx.translate(base.x, base.y);
            ctx.rotate(ang);

            const hipX = 0;
            const hipY = side * radii[idx] * 0.7;
            const kneeX = (-7 + Math.cos(swing) * 3) * legScale;
            const kneeY = side * (radii[idx] + 10 * legScale);
            const pawX = (7 + Math.sin(swing) * 4) * legScale;
            const pawY = side * (radii[idx] + 15 * legScale);

            // Bắp chân + cẳng chân
            ctx.beginPath();
            ctx.moveTo(hipX, hipY);
            ctx.quadraticCurveTo(kneeX, kneeY, pawX, pawY);
            ctx.strokeStyle = theme.mid;
            ctx.lineWidth = 4.2 * legScale;
            ctx.shadowColor = theme.glow;
            ctx.shadowBlur = 8 * legScale;
            ctx.stroke();

            // Khớp lửa ở khuỷu chân
            ctx.beginPath();
            ctx.moveTo(kneeX, kneeY);
            ctx.lineTo(kneeX - 9 * legScale, kneeY + side * 4 * legScale);
            ctx.strokeStyle = theme.mane;
            ctx.lineWidth = 1.8 * legScale;
            ctx.stroke();

            // 3 móng vuốt sắc nhọn
            ctx.strokeStyle = theme.belly;
            ctx.lineWidth = 1.6 * legScale;
            [-0.4, 0, 0.4].forEach(clawSpread => {
                ctx.beginPath();
                ctx.moveTo(pawX, pawY);
                ctx.lineTo(
                    pawX + Math.cos(clawSpread) * 7 * legScale,
                    pawY + (Math.sin(clawSpread) * 5 + side * 2) * legScale
                );
                ctx.stroke();
            });
            ctx.restore();
        });
    });

    // --- C. THÂN RỒNG LIỀN KHỐI (MESH + GRADIENT) ---
    ctx.beginPath();
    ctx.moveTo(leftPts[0].x, leftPts[0].y);
    for (let i = 1; i < n; i++) {
        ctx.lineTo(leftPts[i].x, leftPts[i].y);
    }
    for (let i = n - 1; i >= 0; i--) {
        ctx.lineTo(rightPts[i].x, rightPts[i].y);
    }
    ctx.closePath();

    const bodyGrad = ctx.createLinearGradient(
        segments[0].x, segments[0].y,
        segments[n - 1].x, segments[n - 1].y
    );
    bodyGrad.addColorStop(0, theme.light);
    bodyGrad.addColorStop(0.25, theme.mid);
    bodyGrad.addColorStop(0.75, theme.core);
    bodyGrad.addColorStop(1, theme.mid);

    ctx.fillStyle = bodyGrad;
    ctx.shadowColor = theme.glow;
    ctx.shadowBlur = 18 * scale;
    ctx.fill();

    // Viền ánh kim bọc quanh thân
    ctx.strokeStyle = theme.light;
    ctx.lineWidth = 1.2 * scale;
    ctx.globalAlpha = 0.75;
    ctx.stroke();
    ctx.globalAlpha = 1;

    // --- D. VẢY RỒNG XẾP LỚP & VÂY SỐNG LƯNG ---
    ctx.shadowBlur = 0;
    for (let i = n - 3; i >= 2; i--) {
        const s = segments[i];
        const r = radii[i];
        const ang = angles[i];

        ctx.save();
        ctx.translate(s.x, s.y);
        ctx.rotate(ang);

        // Vảy hình bán nguyệt xếp lớp
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.72, -Math.PI * 0.65, Math.PI * 0.65);
        ctx.strokeStyle = i % 2 === 0 ? theme.light : theme.belly;
        ctx.lineWidth = Math.max(0.8, 1.3 * scale);
        ctx.globalAlpha = 0.55;
        ctx.stroke();

        // Gai vây sống lưng phát sáng
        if (i < n - 4) {
            const finWave = Math.sin(time * 0.015 - i * 0.5) * 2.5 * scale;
            ctx.beginPath();
            ctx.moveTo(r * 0.25, 0);
            ctx.quadraticCurveTo(-r * 0.35, finWave, -r * 1.15, finWave * 1.2);
            ctx.strokeStyle = theme.mane;
            ctx.lineWidth = Math.max(1, 2.1 * scale * (1 - i / n));
            ctx.globalAlpha = 0.85;
            ctx.stroke();
        }
        ctx.restore();
    }

    // --- E. ĐẦU RỒNG PHƯƠNG ĐÔNG (BỜM, SỪNG HƯƠU, MÕM, RÂU, MẮT) ---
    const head = segments[0];
    const headAngle = angles[0];

    ctx.save();
    ctx.translate(head.x, head.y);
    ctx.rotate(headAngle);
    ctx.globalAlpha = 1;

    // 1. Bờm rồng rực cháy tỏa ra sau cổ
    ctx.shadowColor = theme.glow;
    ctx.shadowBlur = 14 * scale;
    for (let m = -4; m <= 4; m++) {
        const spread = m * 0.26;
        const len = (22 - Math.abs(m) * 1.8) * scale;
        const wave = Math.sin(time * 0.014 + m) * 3.5 * scale;
        ctx.beginPath();
        ctx.moveTo(-4 * scale, m * 2 * scale);
        ctx.quadraticCurveTo(
            -len * 0.6, m * 5 * scale + wave,
            -len, Math.sin(spread) * len * 0.85 - wave * 0.5
        );
        ctx.strokeStyle = Math.abs(m) % 2 === 0 ? theme.mane : theme.light;
        ctx.lineWidth = (2.8 - Math.abs(m) * 0.3) * scale;
        ctx.stroke();
    }

    // 2. Sừng hươu phân nhánh (Đặc trưng rồng Á Đông)
    ctx.strokeStyle = theme.belly;
    ctx.lineWidth = 2.6 * scale;
    ctx.shadowColor = theme.light;
    ctx.shadowBlur = 10 * scale;
    [-1, 1].forEach(side => {
        // Nhánh chính cong ra sau
        ctx.beginPath();
        ctx.moveTo(-2 * scale, side * 5 * scale);
        ctx.bezierCurveTo(
            -12 * scale, side * 14 * scale,
            -22 * scale, side * 18 * scale,
            -32 * scale, side * 12 * scale
        );
        ctx.stroke();

        // Nhánh phụ chĩa lên
        ctx.beginPath();
        ctx.moveTo(-14 * scale, side * 13 * scale);
        ctx.quadraticCurveTo(-18 * scale, side * 22 * scale, -24 * scale, side * 23 * scale);
        ctx.lineWidth = 1.9 * scale;
        ctx.stroke();
        ctx.lineWidth = 2.6 * scale;
    });

    // 3. Khối đầu & mõm rồng uy nghiêm
    const headGrad = ctx.createLinearGradient(-12 * scale, 0, 26 * scale, 0);
    headGrad.addColorStop(0, theme.core);
    headGrad.addColorStop(0.55, theme.mid);
    headGrad.addColorStop(1, theme.light);

    ctx.beginPath();
    ctx.moveTo(25 * scale, 0); // Chóp mũi
    ctx.quadraticCurveTo(22 * scale, -6.5 * scale, 13 * scale, -6.5 * scale); // Sống mõm trên
    ctx.quadraticCurveTo(6 * scale, -12 * scale, -6 * scale, -11 * scale);    // Gò má trên
    ctx.lineTo(-13 * scale, -5 * scale);
    ctx.lineTo(-10 * scale, 0);
    ctx.lineTo(-13 * scale, 5 * scale);
    ctx.lineTo(-6 * scale, 11 * scale);                                       // Gò má dưới
    ctx.quadraticCurveTo(6 * scale, 12 * scale, 13 * scale, 6.5 * scale);
    ctx.quadraticCurveTo(22 * scale, 6.5 * scale, 25 * scale, 0);
    ctx.closePath();

    ctx.fillStyle = headGrad;
    ctx.shadowColor = theme.glow;
    ctx.shadowBlur = 18 * scale;
    ctx.fill();
    ctx.strokeStyle = theme.belly;
    ctx.lineWidth = 1.2 * scale;
    ctx.stroke();

    // Đường sống mũi & lỗ mũi
    ctx.fillStyle = theme.core;
    [-1, 1].forEach(side => {
        ctx.beginPath();
        ctx.ellipse(20 * scale, side * 2.5 * scale, 1.8 * scale, 0.9 * scale, side * 0.3, 0, Math.PI * 2);
        ctx.fill();
    });

    // 4. Râu rồng dài uốn lượn mềm mại
    ctx.strokeStyle = theme.light;
    ctx.lineWidth = 1.7 * scale;
    ctx.shadowColor = theme.glow;
    ctx.shadowBlur = 10 * scale;
    const w1 = Math.sin(time * 0.011) * 7 * scale;
    const w2 = Math.cos(time * 0.011) * 7 * scale;
    [-1, 1].forEach(side => {
        ctx.beginPath();
        ctx.moveTo(19 * scale, side * 4.5 * scale);
        ctx.bezierCurveTo(
            26 * scale, side * 14 * scale + w1,
            8 * scale, side * 26 * scale + w2,
            -14 * scale, side * 30 * scale - w1
        );
        ctx.stroke();
    });

    // 5. Đôi mắt Thần Long sắc bén phát sáng
    ctx.shadowColor = '#ffffff';
    ctx.shadowBlur = 12 * scale;
    [-1, 1].forEach(side => {
        // Viền lông mày rồng
        ctx.beginPath();
        ctx.moveTo(9 * scale, side * 3.2 * scale);
        ctx.lineTo(1 * scale, side * 8.2 * scale);
        ctx.strokeStyle = theme.belly;
        ctx.lineWidth = 1.8 * scale;
        ctx.stroke();

        // Tròng mắt phát sáng
        ctx.beginPath();
        ctx.ellipse(5.5 * scale, side * 5.2 * scale, 3.6 * scale, 1.8 * scale, side * 0.3, 0, Math.PI * 2);
        ctx.fillStyle = theme.eye;
        ctx.fill();

        // Con ngươi dọc sắc lẹm
        ctx.beginPath();
        ctx.ellipse(6.2 * scale, side * 5.2 * scale, 1.1 * scale, 1.6 * scale, 0, 0, Math.PI * 2);
        ctx.fillStyle = '#09090b';
        ctx.fill();
    });

    ctx.restore();
    ctx.restore();
}

// =========================================================================
// 3. HOẠT ẢNH KIM LONG XUẤT THẾ KHI ĐỘT PHÁ CẢNH GIỚI
// =========================================================================
function playDragonBreakthrough(level, onPeakCallback) {
    const canvas = document.createElement('canvas');
    canvas.style.cssText = 'position:fixed;inset:0;width:100vw;height:100vh;pointer-events:none;z-index:10000;';
    document.body.appendChild(canvas);

    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    const theme = getDragonTheme(level);
    const numSegments = 46;
    const segDist = 11.5;
    const segments = Array.from({ length: numSegments }, () => ({ x: -180, y: canvas.height * 0.78 }));
    const particles = [];

    const duration = 2900;
    const startTime = performance.now();
    let modalTriggered = false;

    function animate(now) {
        const elapsed = now - startTime;
        const progress = Math.min(1, elapsed / duration);

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Đường bay uốn lượn uyển chuyển chéo màn hình
        const headX = -160 + (canvas.width + 520) * progress;
        const baseY = canvas.height * (0.76 - 0.50 * progress);
        const headY = baseY + Math.sin(progress * Math.PI * 3.8) * (canvas.height * 0.16);

        segments[0].x = headX;
        segments[0].y = headY;

        for (let i = 1; i < numSegments; i++) {
            const prev = segments[i - 1];
            const curr = segments[i];
            const dx = prev.x - curr.x;
            const dy = prev.y - curr.y;
            const dist = Math.hypot(dx, dy) || 1;
            curr.x = prev.x - (dx / dist) * segDist;
            curr.y = prev.y - (dy / dist) * segDist;
        }

        // Hạt linh khí & bụi vàng bay theo thân rồng
        for (let k = 0; k < 5; k++) {
            const pick = segments[Math.floor(Math.random() * 24)];
            particles.push({
                x: pick.x + (Math.random() - 0.5) * 34,
                y: pick.y + (Math.random() - 0.5) * 34,
                vx: (Math.random() - 0.5) * 2.8 - 0.8,
                vy: (Math.random() - 0.5) * 2.8 - 0.9,
                life: 1,
                size: Math.random() * 4.5 + 1.5,
                color: Math.random() > 0.4 ? theme.glow : theme.belly
            });
        }

        // Vẽ hào quang linh khí (Additive blending cho rực sáng)
        ctx.save();
        ctx.globalCompositeOperation = 'lighter';
        for (let i = particles.length - 1; i >= 0; i--) {
            const p = particles[i];
            p.x += p.vx;
            p.y += p.vy;
            p.life -= 0.022;
            if (p.life <= 0) {
                particles.splice(i, 1);
                continue;
            }
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size * p.life, 0, Math.PI * 2);
            ctx.fillStyle = p.color;
            ctx.globalAlpha = p.life * 0.85;
            ctx.fill();
        }
        ctx.restore();

        // Vẽ Thần Long cỡ lớn
        drawEasternDragon(ctx, segments, theme, 1.45, now);

        if (!modalTriggered && progress >= 0.36) {
            modalTriggered = true;
            if (typeof onPeakCallback === 'function') onPeakCallback();
        }

        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            canvas.remove();
        }
    }

    requestAnimationFrame(animate);
}

// =========================================================================
// 4. RỒNG HỘ MỆNH BAY THEO CHUỘT (TỪ NGUYÊN ANH KỲ - LEVEL 4 TRỞ LÊN)
// =========================================================================
function initCursorDragon(level) {
    if (level < 4 || window.innerWidth < 768) return;

    const canvas = document.createElement('canvas');
    canvas.id = 'cursorDragonCanvas';
    canvas.style.cssText = 'position:fixed;inset:0;width:100vw;height:100vh;pointer-events:none;z-index:9998;';
    document.body.appendChild(canvas);

    const ctx = canvas.getContext('2d');
    const resize = () => {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    };
    resize();
    window.addEventListener('resize', resize);

    const theme = getDragonTheme(level);
    const numSegments = 26;
    const segDist = 6.2;
    const segments = Array.from({ length: numSegments }, () => ({ x: -100, y: -100 }));
    const mouse = { x: window.innerWidth / 2, y: window.innerHeight / 2, active: false };
    const sparks = [];

    window.addEventListener('mousemove', (e) => {
        mouse.x = e.clientX;
        mouse.y = e.clientY;
        if (!mouse.active) {
            mouse.active = true;
            segments.forEach(s => { s.x = mouse.x; s.y = mouse.y; });
        }
    });

    function animate(now) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        if (mouse.active) {
            // Quỹ đạo lượn hình số 8 nhẹ quanh con trỏ chuột
            const orbitX = mouse.x + Math.cos(now * 0.0038) * 22;
            const orbitY = mouse.y + Math.sin(now * 0.0076) * 14;
            segments[0].x += (orbitX - segments[0].x) * 0.12;
            segments[0].y += (orbitY - segments[0].y) * 0.12;

            for (let i = 1; i < numSegments; i++) {
                const prev = segments[i - 1];
                const curr = segments[i];
                const dx = prev.x - curr.x;
                const dy = prev.y - curr.y;
                const dist = Math.hypot(dx, dy) || 1;
                curr.x = prev.x - (dx / dist) * segDist;
                curr.y = prev.y - (dy / dist) * segDist;
            }

            // Hạt linh khí bay lơ lửng theo đuôi
            if (Math.random() < 0.55) {
                const tail = segments[Math.floor(Math.random() * 10) + 6];
                sparks.push({
                    x: tail.x + (Math.random() - 0.5) * 8,
                    y: tail.y + (Math.random() - 0.5) * 8,
                    vx: (Math.random() - 0.5) * 0.8,
                    vy: (Math.random() - 0.5) * 0.8 - 0.35,
                    life: 1,
                    size: Math.random() * 2.2 + 0.8
                });
            }

            ctx.save();
            ctx.globalCompositeOperation = 'lighter';
            for (let i = sparks.length - 1; i >= 0; i--) {
                const s = sparks[i];
                s.x += s.vx;
                s.y += s.vy;
                s.life -= 0.032;
                if (s.life <= 0) {
                    sparks.splice(i, 1);
                    continue;
                }
                ctx.beginPath();
                ctx.arc(s.x, s.y, s.size * s.life, 0, Math.PI * 2);
                ctx.fillStyle = theme.glow;
                ctx.globalAlpha = s.life * 0.8;
                ctx.fill();
            }
            ctx.restore();

            drawEasternDragon(ctx, segments, theme, 0.55, now);
        }

        requestAnimationFrame(animate);
    }

    requestAnimationFrame(animate);
}

// =========================================================================
// 5. MODAL THÔNG BÁO ĐỘT PHÁ CẢNH GIỚI
// =========================================================================
function showBreakthroughModal(realmName, realmTitle, perks, customSubtitle, level) {
    if (document.getElementById('btOverlay')) return;

    const renderModal = () => {
        if (document.getElementById('btOverlay')) return;
        const overlay = document.createElement('div');
        overlay.id = 'btOverlay';
        overlay.className = 'breakthrough-overlay';
        overlay.innerHTML = `
            <div class="breakthrough-ring"></div>
            <div class="breakthrough-card">
                <div class="breakthrough-heading">
                    <i class="bi bi-stars me-1"></i> ${customSubtitle || 'Đột Phá Cảnh Giới Thành Công'} <i class="bi bi-stars ms-1"></i>
                </div>
                <div class="breakthrough-realm my-2">${realmName}</div>
                <div class="mb-3">
                    <span class="realm-title-tag">Danh hiệu: 「${realmTitle}」</span>
                </div>
                <p class="small text-light opacity-75 mb-4">${perks || ''}</p>
                <button type="button" class="btn btn-warning btn-sm fw-semibold px-4" onclick="document.getElementById('btOverlay').remove()">
                    Lĩnh Ngộ
                </button>
            </div>
        `;
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) overlay.remove();
        });
        document.body.appendChild(overlay);
    };

    playDragonBreakthrough(level || 8, renderModal);
}

// =========================================================================
// 6. TỰ ĐỘNG KÍCH HOẠT KHI TẢI TRANG
// =========================================================================
document.addEventListener('DOMContentLoaded', function () {
    const tracker = document.getElementById('realm-tracker');
    if (tracker) {
        const userId = tracker.dataset.userId;
        const currentLevel = parseInt(tracker.dataset.level, 10);
        const realmName = tracker.dataset.name;
        const realmTitle = tracker.dataset.title;
        const perks = tracker.dataset.perks;

        if (!isNaN(currentLevel)) {
            initCursorDragon(currentLevel);

            if (userId) {
                const key = 'bookstore_realm_level_' + userId;
                const saved = localStorage.getItem(key);
                if (saved !== null && currentLevel !== parseInt(saved, 10)) {
                    const subtitle = currentLevel > parseInt(saved, 10)
                        ? 'Đột Phá Cảnh Giới Thành Công'
                        : 'Cảnh Giới Đã Được Điều Chỉnh';
                    showBreakthroughModal(realmName, realmTitle, perks, subtitle, currentLevel);
                }
                localStorage.setItem(key, String(currentLevel));
            }
        }
    }

    const adminTrigger = document.getElementById('admin-levelup-trigger');
    if (adminTrigger && adminTrigger.dataset.name) {
        showBreakthroughModal(
            adminTrigger.dataset.name,
            adminTrigger.dataset.title,
            adminTrigger.dataset.perks,
            'Đã Cập Nhật Cảnh Giới Cho ' + adminTrigger.dataset.targetUser,
            8
        );
    }
});