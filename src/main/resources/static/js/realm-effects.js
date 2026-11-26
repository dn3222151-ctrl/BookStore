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
// 2. ÂM THANH ĐỘT PHÁ BẰNG WEB AUDIO API (CHUÔNG ĐỒNG + SẤM RỀN)
// =========================================================================
function playBreakthroughAudio() {
    try {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        const ctx = new AudioCtx();
        const now = ctx.currentTime;

        // 1. TIẾNG NỔ THIÊN LÔI (Dày & to hơn: Sub-bass BÙM + Sấm rền)
        // Lớp trầm (Sub-bass drop) tạo lực nổ êm, không vỡ tiếng
        const subOsc = ctx.createOscillator();
        const subGain = ctx.createGain();
        subOsc.type = 'sine';
        subOsc.frequency.setValueAtTime(130, now);
        subOsc.frequency.exponentialRampToValueAtTime(32, now + 0.45);
        subGain.gain.setValueAtTime(0.65, now);
        subGain.gain.exponentialRampToValueAtTime(0.001, now + 0.55);
        subOsc.connect(subGain);
        subGain.connect(ctx.destination);
        subOsc.start(now);
        subOsc.stop(now + 0.58);

        // Lớp sấm rền (Filtered noise)
        const bufferSize = ctx.sampleRate * 0.95;
        const noiseBuffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
        const output = noiseBuffer.getChannelData(0);
        for (let i = 0; i < bufferSize; i++) {
            output[i] = (Math.random() * 2 - 1) * Math.exp(-i / (ctx.sampleRate * 0.28));
        }
        const noise = ctx.createBufferSource();
        noise.buffer = noiseBuffer;

        const filter = ctx.createBiquadFilter();
        filter.type = 'lowpass';
        filter.frequency.setValueAtTime(620, now);
        filter.frequency.exponentialRampToValueAtTime(140, now + 0.85);

        const thunderGain = ctx.createGain();
        thunderGain.gain.setValueAtTime(0.55, now);
        thunderGain.gain.exponentialRampToValueAtTime(0.001, now + 0.92);

        noise.connect(filter);
        filter.connect(thunderGain);
        thunderGain.connect(ctx.destination);
        noise.start(now);

        // 2. TIẾNG CHUÔNG CỔ NGÂN VANG NHẸ NHÀNG (Nở ra ngay sau tiếng nổ)
        const bellStart = now + 0.1;
        [392.0, 587.33, 783.99, 1174.66].forEach((freq, idx) => {
            const osc = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.type = 'sine';
            osc.frequency.setValueAtTime(freq, bellStart);

            const peak = 0.13 / (idx + 1);
            gain.gain.setValueAtTime(0.0001, bellStart);
            gain.gain.linearRampToValueAtTime(peak, bellStart + 0.06);
            gain.gain.exponentialRampToValueAtTime(0.0001, bellStart + 3.2 - idx * 0.45);

            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.start(bellStart);
            osc.stop(bellStart + 3.3);
        });
    } catch (e) {

    }
}

// =========================================================================
// 3. MÀN HÌNH CHỜ XUYÊN KHÔNG (TRẬN PHÁP TU TIÊN + HỐ ĐEN KHÔNG GIAN)
// =========================================================================
// =========================================================================
// 3. MÀN HÌNH CHỜ CỔ TRANG TU TIÊN (THỦY MẶC SƠN THỦY + HOA ĐÀO + KHAI MÔN)
// =========================================================================
function playXuyenKhongIntro(forceReplay) {
    if (!forceReplay && sessionStorage.getItem('bookstore_xuyenkhong_seen')) return;
    if (document.getElementById('xuyenKhongOverlay')) return;

    sessionStorage.setItem('bookstore_xuyenkhong_seen', '1');

    const overlay = document.createElement('div');
    overlay.id = 'xuyenKhongOverlay';
    overlay.className = 'xuyenkhong-overlay';
    overlay.innerHTML = `
        <div class="xk-door xk-door-left"></div>
        <div class="xk-door xk-door-right"></div>
        <canvas class="xuyenkhong-canvas" id="xkCanvas"></canvas>
        <button type="button" class="xuyenkhong-skip" id="xkSkipBtn">Bỏ qua</button>
        <div class="xuyenkhong-ui">
            <div class="xuyenkhong-badge">Vạn Quyển Tàng Kinh</div>
            <div class="xk-title-row">
                <div class="xuyenkhong-title">THƯ CÁC BOOKSTORE</div>
                <div class="xk-seal">Tiên<br>Các</div>
            </div>
            <div class="xuyenkhong-sub" id="xkStatus">Vén mây khai mở Tàng Kinh Các...</div>
            <div class="xuyenkhong-bar-wrap">
                <div class="xuyenkhong-bar-fill" id="xkBarFill"></div>
            </div>
            <div class="xuyenkhong-percent" id="xkPercent">0%</div>
        </div>
    `;
    document.body.appendChild(overlay);

    const canvas = document.getElementById('xkCanvas');
    const ctx = canvas.getContext('2d');
    const statusEl = document.getElementById('xkStatus');
    const barFill = document.getElementById('xkBarFill');
    const percentEl = document.getElementById('xkPercent');

    let w = (canvas.width = window.innerWidth);
    let h = (canvas.height = window.innerHeight);
    let closed = false;
    let rafId = null;

    const onResize = () => {
        w = canvas.width = window.innerWidth;
        h = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', onResize);

    // Đom đóm linh khí vàng bay nhẹ lên trời
    const spiritMotes = Array.from({ length: 48 }, () => ({
        x: Math.random() * w,
        y: Math.random() * h,
        r: Math.random() * 2.2 + 0.8,
        vy: -(Math.random() * 0.55 + 0.25),
        vx: (Math.random() - 0.5) * 0.35,
        phase: Math.random() * Math.PI * 2
    }));

    // Cánh hoa đào cổ trang rơi chéo nhẹ trong gió
    const petals = Array.from({ length: 24 }, () => ({
        x: Math.random() * w,
        y: Math.random() * h,
        size: Math.random() * 6 + 4,
        vx: Math.random() * 0.9 + 0.4,
        vy: Math.random() * 0.8 + 0.5,
        rot: Math.random() * Math.PI * 2,
        vRot: (Math.random() - 0.5) * 0.035,
        sway: Math.random() * Math.PI * 2
    }));

    // Dải sương mù tiên cảnh trôi ngang chân núi
    const mists = Array.from({ length: 7 }, (_, i) => ({
        x: (w / 6) * i,
        y: h * (0.68 + (i % 3) * 0.08),
        rx: w * (0.26 + (i % 2) * 0.12),
        ry: h * 0.11,
        speed: 0.25 + (i % 3) * 0.14
    }));

    const finishIntro = () => {
        if (closed) return;
        closed = true;
        window.removeEventListener('resize', onResize);
        overlay.classList.add('xk-exit');
        setTimeout(() => {
            if (rafId) cancelAnimationFrame(rafId);
            overlay.remove();
        }, 980);
    };

    document.getElementById('xkSkipBtn').addEventListener('click', finishIntro);

    const duration = 2700; // 2.7 giây êm ái
    const startTime = performance.now();

    function drawMountainLayer(baseY, amp, freq, phase, color) {
        ctx.beginPath();
        ctx.moveTo(0, h);
        for (let x = 0; x <= w; x += 24) {
            const y = baseY
                - Math.sin(x * freq + phase) * amp
                - Math.cos(x * freq * 2.3 + phase * 1.4) * (amp * 0.38);
            ctx.lineTo(x, y);
        }
        ctx.lineTo(w, h);
        ctx.closePath();
        ctx.fillStyle = color;
        ctx.fill();
    }

    function drawAncientSealRing(now, progress) {
        const cx = w / 2;
        const cy = h / 2;
        const r = Math.min(w, h) * 0.28;

        ctx.save();
        ctx.translate(cx, cy);

        // Vầng trăng / hào quang mờ sau chữ
        const moonGrad = ctx.createRadialGradient(0, 0, r * 0.1, 0, 0, r * 1.25);
        moonGrad.addColorStop(0, `rgba(245, 217, 142, ${0.14 + progress * 0.1})`);
        moonGrad.addColorStop(0.55, 'rgba(212, 175, 55, 0.04)');
        moonGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
        ctx.fillStyle = moonGrad;
        ctx.beginPath();
        ctx.arc(0, 0, r * 1.25, 0, Math.PI * 2);
        ctx.fill();

        // Vòng cổ phong nét mảnh xoay cực chậm
        ctx.save();
        ctx.rotate(now * 0.00022);
        ctx.strokeStyle = 'rgba(212, 175, 55, 0.32)';
        ctx.lineWidth = 1.2;
        ctx.beginPath();
        ctx.arc(0, 0, r, 0, Math.PI * 2);
        ctx.stroke();

        ctx.setLineDash([4, 10]);
        ctx.strokeStyle = 'rgba(245, 217, 142, 0.26)';
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.9, 0, Math.PI * 2);
        ctx.stroke();
        ctx.setLineDash([]);

        // 8 quẻ Bát Quái mảnh, nhẹ nhàng quanh vòng tròn
        for (let i = 0; i < 8; i++) {
            ctx.save();
            ctx.rotate((i * Math.PI) / 4);
            ctx.strokeStyle = 'rgba(230, 193, 108, 0.42)';
            ctx.lineWidth = 1.6;
            for (let b = 0; b < 3; b++) {
                const y = -r * 0.96 + b * 4.5;
                if ((i + b) % 2 === 0) {
                    ctx.beginPath();
                    ctx.moveTo(-8, y);
                    ctx.lineTo(8, y);
                    ctx.stroke();
                } else {
                    ctx.beginPath();
                    ctx.moveTo(-8, y);
                    ctx.lineTo(-2, y);
                    ctx.moveTo(2, y);
                    ctx.lineTo(8, y);
                    ctx.stroke();
                }
            }
            ctx.restore();
        }
        ctx.restore();

        // Vòng trong xoay ngược thật chậm
        ctx.save();
        ctx.rotate(-now * 0.0003);
        ctx.strokeStyle = 'rgba(212, 175, 55, 0.18)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.arc(0, 0, r * 0.74, 0, Math.PI * 2);
        ctx.stroke();
        ctx.restore();

        ctx.restore();
    }

    function animate(now) {
        if (closed) return;
        const elapsed = Math.max(0, now - startTime);
        const progress = Math.min(1, elapsed / duration);
        const pct = Math.floor(progress * 100);

        ctx.clearRect(0, 0, w, h);

        // 1. Vòng cổ phong mờ phía sau tiêu đề
        drawAncientSealRing(now, progress);

        // 2. Ba lớp núi thủy mặc (Ink-wash mountains)
        drawMountainLayer(h * 0.77, h * 0.09, 0.0022, 0.8, 'rgba(22, 34, 31, 0.55)');
        drawMountainLayer(h * 0.84, h * 0.075, 0.0031, 2.5, 'rgba(14, 22, 20, 0.72)');
        drawMountainLayer(h * 0.91, h * 0.055, 0.0042, 4.1, 'rgba(8, 13, 12, 0.88)');

        // 3. Mây tiên / sương mù trôi chậm
        for (let i = 0; i < mists.length; i++) {
            const m = mists[i];
            m.x += m.speed;
            if (m.x - m.rx > w) m.x = -m.rx;

            const mg = ctx.createRadialGradient(m.x, m.y, 10, m.x, m.y, m.rx);
            mg.addColorStop(0, 'rgba(226, 232, 224, 0.08)');
            mg.addColorStop(1, 'rgba(226, 232, 224, 0)');
            ctx.save();
            ctx.scale(1, m.ry / m.rx);
            ctx.fillStyle = mg;
            ctx.beginPath();
            ctx.arc(m.x, m.y * (m.rx / m.ry), m.rx, 0, Math.PI * 2);
            ctx.fill();
            ctx.restore();
        }

        // 4. Đom đóm linh khí vàng lơ lửng
        for (let i = 0; i < spiritMotes.length; i++) {
            const s = spiritMotes[i];
            s.y += s.vy;
            s.x += s.vx + Math.sin(now * 0.0015 + s.phase) * 0.25;
            if (s.y < -10) {
                s.y = h + 10;
                s.x = Math.random() * w;
            }
            const alpha = 0.25 + 0.55 * (0.5 + 0.5 * Math.sin(now * 0.003 + s.phase));
            ctx.beginPath();
            ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(245, 217, 142, ${alpha})`;
            ctx.shadowColor = '#f5d98e';
            ctx.shadowBlur = 8;
            ctx.fill();
        }
        ctx.shadowBlur = 0;

        // 5. Cánh hoa đào rơi nhẹ
        for (let i = 0; i < petals.length; i++) {
            const p = petals[i];
            p.x += p.vx + Math.sin(now * 0.002 + p.sway) * 0.45;
            p.y += p.vy;
            p.rot += p.vRot;
            if (p.y > h + 20 || p.x > w + 20) {
                p.y = -15;
                p.x = Math.random() * w * 0.85;
            }

            ctx.save();
            ctx.translate(p.x, p.y);
            ctx.rotate(p.rot);
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.bezierCurveTo(p.size * 0.6, -p.size * 0.5, p.size, p.size * 0.3, 0, p.size);
            ctx.bezierCurveTo(-p.size, p.size * 0.3, -p.size * 0.6, -p.size * 0.5, 0, 0);
            ctx.fillStyle = 'rgba(251, 207, 232, 0.58)';
            ctx.fill();
            ctx.restore();
        }

        // 6. Cập nhật tiến trình & lời dẫn cổ trang
        barFill.style.width = pct + '%';
        percentEl.textContent = pct + '%';
        if (pct < 38) {
            statusEl.textContent = 'Vén mây khai mở Tàng Kinh Các...';
        } else if (pct < 78) {
            statusEl.textContent = 'Tụ hội linh khí vạn quyển kỳ thư...';
        } else {
            statusEl.textContent = 'Cổng Tiên Các đã mở — Mời Đạo Hữu nhập môn';
        }

        if (progress < 1) {
            rafId = requestAnimationFrame(animate);
        } else {
            finishIntro();
        }
    }

    rafId = requestAnimationFrame(animate);
}

// =========================================================================
// 4. VẼ THIÊN LÔI PHÂN NHÁNH (HEAVENLY LIGHTNING)
// =========================================================================
function drawLightningBolt(ctx, x1, y1, x2, y2, displace, color, width) {
    if (displace < 6) {
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.strokeStyle = color;
        ctx.lineWidth = width;
        ctx.stroke();
        return;
    }
    const midX = (x1 + x2) / 2 + (Math.random() - 0.5) * displace;
    const midY = (y1 + y2) / 2 + (Math.random() - 0.5) * displace * 0.4;
    drawLightningBolt(ctx, x1, y1, midX, midY, displace / 1.9, color, width);
    drawLightningBolt(ctx, midX, midY, x2, y2, displace / 1.9, color, width);

    if (Math.random() < 0.38 && width > 1.2) {
        const bx = midX + (Math.random() - 0.5) * displace * 1.4;
        const by = midY + Math.abs((y2 - y1) * 0.35);
        drawLightningBolt(ctx, midX, midY, bx, by, displace / 2.2, color, width * 0.55);
    }
}

// =========================================================================
// 5. HÀM VẼ THẦN LONG PHƯƠNG ĐÔNG (THÂN LIỀN KHỐI, VẢY, SỪNG HƯƠU, 4 CHÂN)
// =========================================================================
function drawEasternDragon(ctx, segments, theme, scale, time) {
    const n = segments.length;
    if (n < 6) return;

    const angles = new Array(n);
    const radii = new Array(n);
    const leftPts = new Array(n);
    const rightPts = new Array(n);

    for (let i = 0; i < n; i++) {
        const prev = segments[Math.max(0, i - 1)];
        const next = segments[Math.min(n - 1, i + 1)];
        angles[i] = Math.atan2(prev.y - next.y, prev.x - next.x);

        const t = i / (n - 1);
        const profile = t < 0.12
            ? 0.72 + (t / 0.12) * 0.28
            : Math.pow(1 - (t - 0.12) / 0.88, 0.85);
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

    // Chùm lông đuôi rực lửa
    const tail = segments[n - 1];
    const tailAngle = angles[n - 1] + Math.PI;
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

    // Bốn chân rồng & móng vuốt
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

            const hipY = side * radii[idx] * 0.7;
            const kneeX = (-7 + Math.cos(swing) * 3) * legScale;
            const kneeY = side * (radii[idx] + 10 * legScale);
            const pawX = (7 + Math.sin(swing) * 4) * legScale;
            const pawY = side * (radii[idx] + 15 * legScale);

            ctx.beginPath();
            ctx.moveTo(0, hipY);
            ctx.quadraticCurveTo(kneeX, kneeY, pawX, pawY);
            ctx.strokeStyle = theme.mid;
            ctx.lineWidth = 4.2 * legScale;
            ctx.shadowColor = theme.glow;
            ctx.shadowBlur = 8 * legScale;
            ctx.stroke();

            ctx.beginPath();
            ctx.moveTo(kneeX, kneeY);
            ctx.lineTo(kneeX - 9 * legScale, kneeY + side * 4 * legScale);
            ctx.strokeStyle = theme.mane;
            ctx.lineWidth = 1.8 * legScale;
            ctx.stroke();

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

    // Thân rồng liền khối
    ctx.beginPath();
    ctx.moveTo(leftPts[0].x, leftPts[0].y);
    for (let i = 1; i < n; i++) ctx.lineTo(leftPts[i].x, leftPts[i].y);
    for (let i = n - 1; i >= 0; i--) ctx.lineTo(rightPts[i].x, rightPts[i].y);
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

    ctx.strokeStyle = theme.light;
    ctx.lineWidth = 1.2 * scale;
    ctx.globalAlpha = 0.75;
    ctx.stroke();
    ctx.globalAlpha = 1;

    // Vảy xếp lớp & vây sống lưng
    ctx.shadowBlur = 0;
    for (let i = n - 3; i >= 2; i--) {
        const s = segments[i];
        const r = radii[i];
        ctx.save();
        ctx.translate(s.x, s.y);
        ctx.rotate(angles[i]);

        ctx.beginPath();
        ctx.arc(0, 0, r * 0.72, -Math.PI * 0.65, Math.PI * 0.65);
        ctx.strokeStyle = i % 2 === 0 ? theme.light : theme.belly;
        ctx.lineWidth = Math.max(0.8, 1.3 * scale);
        ctx.globalAlpha = 0.55;
        ctx.stroke();

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

    // Đầu rồng Phương Đông
    const head = segments[0];
    ctx.save();
    ctx.translate(head.x, head.y);
    ctx.rotate(angles[0]);
    ctx.globalAlpha = 1;

    // Bờm rồng
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

    // Sừng hươu phân nhánh
    ctx.strokeStyle = theme.belly;
    ctx.lineWidth = 2.6 * scale;
    ctx.shadowColor = theme.light;
    ctx.shadowBlur = 10 * scale;
    [-1, 1].forEach(side => {
        ctx.beginPath();
        ctx.moveTo(-2 * scale, side * 5 * scale);
        ctx.bezierCurveTo(
            -12 * scale, side * 14 * scale,
            -22 * scale, side * 18 * scale,
            -32 * scale, side * 12 * scale
        );
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(-14 * scale, side * 13 * scale);
        ctx.quadraticCurveTo(-18 * scale, side * 22 * scale, -24 * scale, side * 23 * scale);
        ctx.lineWidth = 1.9 * scale;
        ctx.stroke();
        ctx.lineWidth = 2.6 * scale;
    });

    // Khối đầu & mõm
    const headGrad = ctx.createLinearGradient(-12 * scale, 0, 26 * scale, 0);
    headGrad.addColorStop(0, theme.core);
    headGrad.addColorStop(0.55, theme.mid);
    headGrad.addColorStop(1, theme.light);

    ctx.beginPath();
    ctx.moveTo(25 * scale, 0);
    ctx.quadraticCurveTo(22 * scale, -6.5 * scale, 13 * scale, -6.5 * scale);
    ctx.quadraticCurveTo(6 * scale, -12 * scale, -6 * scale, -11 * scale);
    ctx.lineTo(-13 * scale, -5 * scale);
    ctx.lineTo(-10 * scale, 0);
    ctx.lineTo(-13 * scale, 5 * scale);
    ctx.lineTo(-6 * scale, 11 * scale);
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

    // Lỗ mũi
    ctx.fillStyle = theme.core;
    [-1, 1].forEach(side => {
        ctx.beginPath();
        ctx.ellipse(20 * scale, side * 2.5 * scale, 1.8 * scale, 0.9 * scale, side * 0.3, 0, Math.PI * 2);
        ctx.fill();
    });

    // Râu rồng
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

    // Đôi mắt Thần Long
    ctx.shadowColor = '#ffffff';
    ctx.shadowBlur = 12 * scale;
    [-1, 1].forEach(side => {
        ctx.beginPath();
        ctx.moveTo(9 * scale, side * 3.2 * scale);
        ctx.lineTo(1 * scale, side * 8.2 * scale);
        ctx.strokeStyle = theme.belly;
        ctx.lineWidth = 1.8 * scale;
        ctx.stroke();

        ctx.beginPath();
        ctx.ellipse(5.5 * scale, side * 5.2 * scale, 3.6 * scale, 1.8 * scale, side * 0.3, 0, Math.PI * 2);
        ctx.fillStyle = theme.eye;
        ctx.fill();

        ctx.beginPath();
        ctx.ellipse(6.2 * scale, side * 5.2 * scale, 1.1 * scale, 1.6 * scale, 0, 0, Math.PI * 2);
        ctx.fillStyle = '#09090b';
        ctx.fill();
    });

    ctx.restore();
    ctx.restore();
}

// =========================================================================
// 6. ĐỘT PHÁ CẢNH GIỚI: THIÊN LÔI + RUNG MÀN HÌNH + KIM LONG XUẤT THẾ
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

    playBreakthroughAudio();
    document.body.classList.add('realm-shake');
    setTimeout(() => document.body.classList.remove('realm-shake'), 380);

    const duration = 3100;
    const startTime = performance.now();
    let modalTriggered = false;

    // Đảm bảo luôn tự xóa canvas kể cả khi có sự cố ngoài ý muốn
    const cleanupTimer = setTimeout(() => {
        if (canvas.parentNode) canvas.remove();
    }, duration + 600);

    function animate(now) {
        try {
            const elapsed = Math.max(0, now - startTime);
            const progress = Math.min(1, elapsed / duration);

            ctx.clearRect(0, 0, canvas.width, canvas.height);

            // PHA 1: Thiên Lôi giáng xuống + Chớp sáng + Sóng xung kích
            if (elapsed < 480) {
                const flashAlpha = Math.max(0, 0.45 * (1 - elapsed / 480));
                ctx.fillStyle = `rgba(254, 249, 195, ${flashAlpha})`;
                ctx.fillRect(0, 0, canvas.width, canvas.height);

                if (elapsed < 340 && Math.floor(elapsed / 55) % 2 === 0) {
                    ctx.save();
                    ctx.globalCompositeOperation = 'lighter';
                    ctx.shadowColor = theme.glow;
                    ctx.shadowBlur = 22;

                    const targetX = canvas.width * 0.5;
                    const targetY = canvas.height * 0.52;
                    drawLightningBolt(ctx, canvas.width * 0.35, 0, targetX, targetY, 95, '#ffffff', 3.4);
                    drawLightningBolt(ctx, canvas.width * 0.65, 0, targetX, targetY, 95, theme.light, 2.6);
                    ctx.restore();
                }

                const shockR = Math.max(1, (elapsed / 480) * Math.min(canvas.width, canvas.height) * 0.65);
                ctx.save();
                ctx.beginPath();
                ctx.arc(canvas.width * 0.5, canvas.height * 0.52, shockR, 0, Math.PI * 2);
                ctx.strokeStyle = theme.light;
                ctx.lineWidth = Math.max(0.5, 4 * (1 - elapsed / 480));
                ctx.globalAlpha = Math.max(0, 1 - elapsed / 480);
                ctx.stroke();
                ctx.restore();
            }

            // PHA 2: Thần Long xuất thế
            const dragonProg = Math.max(0, (elapsed - 160) / (duration - 160));
            if (dragonProg > 0) {
                const headX = -160 + (canvas.width + 520) * dragonProg;
                const baseY = canvas.height * (0.76 - 0.50 * dragonProg);
                const headY = baseY + Math.sin(dragonProg * Math.PI * 3.8) * (canvas.height * 0.16);

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
                    ctx.arc(p.x, p.y, Math.max(0.1, p.size * p.life), 0, Math.PI * 2);
                    ctx.fillStyle = p.color;
                    ctx.globalAlpha = Math.max(0, p.life * 0.85);
                    ctx.fill();
                }
                ctx.restore();

                drawEasternDragon(ctx, segments, theme, 1.45, now);
            }

            if (!modalTriggered && progress >= 0.38) {
                modalTriggered = true;
                if (typeof onPeakCallback === 'function') onPeakCallback();
            }

            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                clearTimeout(cleanupTimer);
                canvas.remove();
            }
        } catch (err) {
            clearTimeout(cleanupTimer);
            canvas.remove();
            if (!modalTriggered && typeof onPeakCallback === 'function') {
                modalTriggered = true;
                onPeakCallback();
            }
        }
    }

    requestAnimationFrame(animate);
}

// =========================================================================
// 7. RỒNG HỘ MỆNH BAY THEO CHUỘT + CÔNG TẮC BẬT/TẮT
// =========================================================================
let _cursorDragonLevel = 0;

function initCursorDragon(level) {
    _cursorDragonLevel = level;
    if (level < 4 || window.innerWidth < 768) return;
    if (localStorage.getItem('bookstore_dragon_disabled') === '1') return;
    if (document.getElementById('cursorDragonCanvas')) return;

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
        if (!document.getElementById('cursorDragonCanvas')) return;
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        if (mouse.active) {
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

function toggleCursorDragon() {
    const isDisabled = localStorage.getItem('bookstore_dragon_disabled') === '1';
    if (isDisabled) {
        localStorage.removeItem('bookstore_dragon_disabled');
        initCursorDragon(_cursorDragonLevel || 8);
    } else {
        localStorage.setItem('bookstore_dragon_disabled', '1');
        const canvas = document.getElementById('cursorDragonCanvas');
        if (canvas) canvas.remove();
    }
    updateDragonToggleLabel();
}

function updateDragonToggleLabel() {
    const label = document.getElementById('dragonToggleText');
    if (!label) return;
    const isDisabled = localStorage.getItem('bookstore_dragon_disabled') === '1';
    label.textContent = isDisabled ? 'Bật Rồng Hộ Mệnh' : 'Tắt Rồng Hộ Mệnh';
}

// =========================================================================
// 8. MODAL THÔNG BÁO ĐỘT PHÁ CẢNH GIỚI
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
// 9. TỰ ĐỘNG KÍCH HOẠT KHI TẢI TRANG
// =========================================================================
document.addEventListener('DOMContentLoaded', function () {
    // 1. Màn hình chờ Xuyên Không (chỉ chạy 1 lần mỗi phiên trên trang /shop)
    if (window.location.pathname === '/shop' || window.location.pathname === '/') {
        playXuyenKhongIntro(false);
    }

    updateDragonToggleLabel();

    // 2. Theo dõi cảnh giới & Rồng hộ mệnh
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

    // 3. Kích hoạt ngay khi Admin vừa bấm Lưu đổi cảnh giới ở /admin/users
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