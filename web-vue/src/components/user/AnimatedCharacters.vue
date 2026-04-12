<template>
  <div ref="containerRef" :style="{ position: 'relative', width: '550px', height: '400px' }">
    <!-- 增加包裹小人整体尺寸的毛玻璃蒙版底座 -->
    <div :style="{ position: 'absolute', inset: '-20px -20px 0 -20px', background: 'rgba(255, 255, 255, 0.1)', backdropFilter: 'blur(16px)', borderRadius: '32px', border: '1px solid rgba(255, 255, 255, 0.3)', boxShadow: '0 16px 32px rgba(0, 0, 0, 0.15)', zIndex: 0 }"></div>

    <div ref="purpleRef" :style="purpleStyle">
      <div ref="purpleFaceRef" :style="purpleFaceStyle">
        <div class="eyeball" data-max-distance="5" :style="smallEyeStyle">
          <div class="eyeball-pupil" :style="smallPupilStyle" />
        </div>
        <div class="eyeball" data-max-distance="5" :style="smallEyeStyle">
          <div class="eyeball-pupil" :style="smallPupilStyle" />
        </div>
      </div>
      <!-- 蓝色：指南针 -->
      <div style="position:absolute; top:120px; left:-20px; width:60px; height:60px; background:white; border-radius:50%; border:5px solid #e2e8f0; box-shadow:0 8px 16px rgba(0,0,0,0.3); display:flex; align-items:center; justify-content:center;">
        <div style="width:0; height:0; border-left:6px solid transparent; border-right:6px solid transparent; border-bottom:18px solid #ef4444; position:absolute; top:7px;"></div>
        <div style="width:0; height:0; border-left:6px solid transparent; border-right:6px solid transparent; border-top:18px solid #94a3b8; position:absolute; bottom:7px;"></div>
        <div style="width:8px; height:8px; background:#1e293b; border-radius:50%; z-index:2;"></div>
      </div>
      <!-- 蓝色：护照 -->
      <div style="position:absolute; top:80px; right:-30px; width:60px; height:85px; background:#f59e0b; border-radius:8px; box-shadow:0 8px 16px rgba(0,0,0,0.3), inset 0 0 0 2px #fde047; transform:rotate(15deg); display:flex; flex-direction:column; align-items:center; padding-top:12px; z-index:5;">
        <div style="width:24px; height:24px; border-radius:50%; border:2px dashed #fef08a; margin-bottom:8px;"></div>
        <div style="width:30px; height:3px; background:#fef08a; border-radius:2px; margin-bottom:4px;"></div>
        <div style="width:20px; height:3px; background:#fef08a; border-radius:2px;"></div>
      </div>
    </div>

    <div ref="blackRef" :style="blackStyle">
      <div ref="blackFaceRef" :style="blackFaceStyle">
        <div class="eyeball" data-max-distance="4" :style="blackEyeStyle">
          <div class="eyeball-pupil" :style="blackPupilStyle" />
        </div>
        <div class="eyeball" data-max-distance="4" :style="blackEyeStyle">
          <div class="eyeball-pupil" :style="blackPupilStyle" />
        </div>
      </div>
      <!-- 绿色：复古相机 -->
      <div style="position:absolute; top:80px; left:20px; width:80px; height:50px; background:#1f2937; border-radius:8px; box-shadow:0 8px 16px rgba(0,0,0,0.3); z-index:5;">
        <div style="position:absolute; top:0; left:0; width:100%; height:15px; background:#e5e7eb; border-radius:8px 8px 0 0;"></div>
        <div style="position:absolute; top:15px; left:20px; width:40px; height:40px; background:#374151; border-radius:50%; border:4px solid #9ca3af; box-shadow:inset 0 0 10px black; display:flex; align-items:center; justify-content:center;">
          <div style="width:16px; height:16px; background:#111827; border-radius:50%;"></div>
        </div>
        <div style="position:absolute; top:4px; right:10px; width:12px; height:8px; background:#fde047; border-radius:2px;"></div>
      </div>
    </div>

    <div ref="orangeRef" :style="orangeStyle">
      <!-- 红色：草帽 -->
      <div style="position:absolute; top:-25px; left:30px; z-index:10;">
        <div style="position:absolute; top:0; left:30px; width:90px; height:60px; background:#fde047; border-radius:50px 50px 20px 20px; box-shadow:inset -5px -5px 10px rgba(0,0,0,0.1);"></div>
        <div style="position:absolute; top:45px; left:30px; width:90px; height:15px; background:#b91c1c;"></div>
        <div style="position:absolute; top:50px; left:0; width:150px; height:25px; background:#fef08a; border-radius:50%; box-shadow:0 10px 15px rgba(0,0,0,0.3);"></div>
      </div>
      <!-- 红色：背包 -->
      <div style="position:absolute; top:70px; left:-20px; width:50px; height:100px; background:#991b1b; border-radius:25px 10px 10px 25px; box-shadow:inset -5px -5px 10px rgba(0,0,0,0.4); z-index:2;">
         <div style="position:absolute; top:20px; left:-10px; width:20px; height:60px; background:#b91c1c; border-radius:10px; box-shadow:0 4px 6px rgba(0,0,0,0.3);"></div>
      </div>
      <div ref="orangeFaceRef" :style="orangeFaceStyle">
        <div class="pupil" data-max-distance="5" :style="dotPupilStyle" />
        <div class="pupil" data-max-distance="5" :style="dotPupilStyle" />
      </div>
    </div>

    <div ref="yellowRef" :style="yellowStyle">
      <!-- 黄色：地图 -->
      <div style="position:absolute; top:120px; left:20px; width:100px; height:70px; background:#fdf6e3; border-radius:4px; box-shadow:0 8px 16px rgba(0,0,0,0.3); transform:rotate(5deg); z-index:5; display:flex;">
        <div style="flex:1; border-right:1px dashed #d1d5db; padding:5px;">
           <div style="width:20px; height:20px; border-bottom:2px dashed #10b981; border-left:2px dashed #10b981; margin:10px 0 0 10px;"></div>
        </div>
        <div style="flex:1; border-right:1px dashed #d1d5db; position:relative;">
           <div style="position:absolute; top:10px; left:5px; width:8px; height:12px; background:#ef4444; border-radius:50% 50% 0 0;"></div>
        </div>
        <div style="flex:1; padding:5px;">
           <div style="width:20px; height:3px; background:#9ca3af; margin-top:10px;"></div>
           <div style="width:15px; height:3px; background:#9ca3af; margin-top:10px;"></div>
        </div>
      </div>
      <!-- 黄色：墨镜 (顶在头上) -->
      <div style="position:absolute; top:-10px; left:30px; width:80px; height:30px; display:flex; gap:5px; align-items:center; z-index:10; pointer-events:none; transform:rotate(-8deg);">
         <div style="flex:1; height:30px; background:rgba(0,0,0,0.85); border-radius:10px 10px 25px 25px; border:3px solid #b45309; box-shadow:inset 3px 5px 5px rgba(255,255,255,0.3);"></div>
         <div style="width:8px; height:4px; background:#b45309;"></div>
         <div style="flex:1; height:30px; background:rgba(0,0,0,0.85); border-radius:10px 10px 25px 25px; border:3px solid #b45309; box-shadow:inset 3px 5px 5px rgba(255,255,255,0.3);"></div>
      </div>
      <div ref="yellowFaceRef" :style="yellowFaceStyle">
        <div class="pupil" data-max-distance="5" :style="dotPupilStyle" />
        <div class="pupil" data-max-distance="5" :style="dotPupilStyle" />
      </div>
      <div ref="yellowMouthRef" :style="yellowMouthStyle" />
    </div>
  </div>
</template>

<script setup lang="ts">
import gsap from 'gsap';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

interface AnimatedCharactersProps {
  isTyping?: boolean;
  showPassword?: boolean;
  passwordLength?: number;
  isPasswordGuardMode?: boolean;
}

const props = withDefaults(defineProps<AnimatedCharactersProps>(), {
  isTyping: false,
  showPassword: false,
  passwordLength: 0,
  isPasswordGuardMode: false,
});

const containerRef = ref<HTMLDivElement | null>(null);
const mouseRef = ref({ x: 0, y: 0 });
const rafIdRef = ref(0);

const purpleRef = ref<HTMLDivElement | null>(null);
const blackRef = ref<HTMLDivElement | null>(null);
const yellowRef = ref<HTMLDivElement | null>(null);
const orangeRef = ref<HTMLDivElement | null>(null);

const purpleFaceRef = ref<HTMLDivElement | null>(null);
const blackFaceRef = ref<HTMLDivElement | null>(null);
const yellowFaceRef = ref<HTMLDivElement | null>(null);
const orangeFaceRef = ref<HTMLDivElement | null>(null);
const yellowMouthRef = ref<HTMLDivElement | null>(null);

const purpleBlinkTimerRef = ref<ReturnType<typeof setTimeout>>();
const blackBlinkTimerRef = ref<ReturnType<typeof setTimeout>>();
const purplePeekTimerRef = ref<ReturnType<typeof setTimeout>>();
const lookingTimerRef = ref<ReturnType<typeof setTimeout>>();

const isLookingRef = ref(false);
const isHidingPassword = computed(() => props.passwordLength > 0 && !props.showPassword);
const isShowingPassword = computed(() => props.passwordLength > 0 && props.showPassword);

const stateRef = ref({
  isTyping: props.isTyping,
  isHidingPassword: isHidingPassword.value,
  isShowingPassword: isShowingPassword.value,
  isLooking: false,
  isPasswordGuardMode: props.isPasswordGuardMode,
});

const quickToRef = ref<Record<string, gsap.QuickToFunc> | null>(null);

const purpleStyle = {
  position: 'absolute',
  bottom: 0,
  left: '70px',
  width: '180px',
  height: '400px',
  background: 'radial-gradient(circle at 30% 20%, #60a5fa 0%, #3b82f6 40%, #1d4ed8 100%)',
  boxShadow: 'inset -20px -20px 40px rgba(0,0,0,0.2), inset 10px 10px 20px rgba(255,255,255,0.4)',
  borderRadius: '40px 40px 0 0',
  zIndex: 1,
  transformOrigin: 'bottom center',
  willChange: 'transform',
} as const;

const blackStyle = {
  position: 'absolute',
  bottom: 0,
  left: '240px',
  width: '120px',
  height: '310px',
  background: 'radial-gradient(circle at 30% 20%, #34d399 0%, #10b981 40%, #047857 100%)',
  boxShadow: 'inset -15px -15px 30px rgba(0,0,0,0.2), inset 10px 10px 20px rgba(255,255,255,0.4)',
  borderRadius: '35px 35px 0 0',
  zIndex: 2,
  transformOrigin: 'bottom center',
  willChange: 'transform',
} as const;

const orangeStyle = {
  position: 'absolute',
  bottom: 0,
  left: 0,
  width: '240px',
  height: '200px',
  background: 'radial-gradient(circle at 30% 20%, #fb7185 0%, #e11d48 40%, #9f1239 100%)',
  boxShadow: 'inset -20px -20px 40px rgba(0,0,0,0.2), inset 10px 10px 20px rgba(255,255,255,0.4)',
  borderRadius: '120px 120px 0 0',
  zIndex: 3,
  transformOrigin: 'bottom center',
  willChange: 'transform',
} as const;

const yellowStyle = {
  position: 'absolute',
  bottom: 0,
  left: '310px',
  width: '140px',
  height: '230px',
  background: 'radial-gradient(circle at 30% 20%, #fde047 0%, #eab308 40%, #854d0e 100%)',
  boxShadow: 'inset -15px -15px 30px rgba(0,0,0,0.2), inset 10px 10px 20px rgba(255,255,255,0.4)',
  borderRadius: '70px 70px 0 0',
  zIndex: 4,
  transformOrigin: 'bottom center',
  willChange: 'transform',
} as const;

const purpleFaceStyle = { position: 'absolute', display: 'flex', gap: '32px', left: '45px', top: '40px' } as const;
const blackFaceStyle = { position: 'absolute', display: 'flex', gap: '24px', left: '26px', top: '32px' } as const;
const orangeFaceStyle = { position: 'absolute', display: 'flex', gap: '32px', left: '82px', top: '90px' } as const;
const yellowFaceStyle = { position: 'absolute', display: 'flex', gap: '24px', left: '52px', top: '40px' } as const;
const yellowMouthStyle = {
  position: 'absolute',
  width: '80px',
  height: '4px',
  backgroundColor: '#2D2D2D',
  borderRadius: '9999px',
  left: '40px',
  top: '88px',
} as const;

const eyeBase = {
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  overflow: 'hidden',
  willChange: 'height',
} as const;

const smallEyeStyle = { ...eyeBase, width: '18px', height: '18px', backgroundColor: 'white' } as const;
const blackEyeStyle = { ...eyeBase, width: '16px', height: '16px', backgroundColor: 'white' } as const;
const smallPupilStyle = { width: '7px', height: '7px', borderRadius: '50%', backgroundColor: '#2D2D2D', willChange: 'transform' } as const;
const blackPupilStyle = { width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#2D2D2D', willChange: 'transform' } as const;
const dotPupilStyle = { width: '12px', height: '12px', borderRadius: '50%', backgroundColor: '#2D2D2D', willChange: 'transform' } as const;

const calcPos = (el: HTMLElement) => {
  const rect = el.getBoundingClientRect();
  const cx = rect.left + rect.width / 2;
  const cy = rect.top + rect.height / 3;
  const dx = mouseRef.value.x - cx;
  const dy = mouseRef.value.y - cy;
  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-6, Math.min(6, -dx / 120)),
  };
};

const calcEyePos = (el: HTMLElement, maxDist: number) => {
  const r = el.getBoundingClientRect();
  const cx = r.left + r.width / 2;
  const cy = r.top + r.height / 2;
  const dx = mouseRef.value.x - cx;
  const dy = mouseRef.value.y - cy;
  const dist = Math.min(Math.sqrt(dx ** 2 + dy ** 2), maxDist);
  const angle = Math.atan2(dy, dx);
  return { x: Math.cos(angle) * dist, y: Math.sin(angle) * dist };
};

const applyLookAtEachOther = () => {
  const qt = quickToRef.value;
  if (qt) {
    qt.purpleFaceLeft(55);
    qt.purpleFaceTop(65);
    qt.blackFaceLeft(32);
    qt.blackFaceTop(12);
  }
  purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: 3, y: 4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
  });
  blackRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: 0, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
  });
};

const applyHidingPassword = () => {
  const qt = quickToRef.value;
  if (qt) {
    qt.purpleFaceLeft(55);
    qt.purpleFaceTop(65);
  }
};

const applyShowPassword = () => {
  const qt = quickToRef.value;
  if (qt) {
    qt.purpleSkew(0);
    qt.blackSkew(0);
    qt.orangeSkew(0);
    qt.yellowSkew(0);
    qt.purpleX(0);
    qt.blackX(0);
    qt.purpleHeight(400);
    qt.purpleFaceLeft(20);
    qt.purpleFaceTop(35);
    qt.blackFaceLeft(10);
    qt.blackFaceTop(28);
    qt.orangeFaceX(50 - 82);
    qt.orangeFaceY(85 - 90);
    qt.yellowFaceX(20 - 52);
    qt.yellowFaceY(35 - 40);
    qt.mouthX(10 - 40);
    qt.mouthY(0);
  }

  purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: -4, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
  });
  blackRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: -4, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
  });
  orangeRef.value?.querySelectorAll('.pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
  });
  yellowRef.value?.querySelectorAll('.pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
  });
};

const applyPasswordGuardMode = () => {
  const qt = quickToRef.value;
  if (qt) {
    qt.purpleSkew(0);
    qt.blackSkew(0);
    qt.orangeSkew(0);
    qt.yellowSkew(0);
    qt.purpleX(0);
    qt.blackX(0);
    qt.purpleHeight(400);
    qt.purpleFaceLeft(24);
    qt.purpleFaceTop(22);
    qt.blackFaceLeft(14);
    qt.blackFaceTop(20);
    qt.orangeFaceX(22 - 82);
    qt.orangeFaceY(72 - 90);
    qt.yellowFaceX(12 - 52);
    qt.yellowFaceY(22 - 40);
    qt.mouthX(-14);
    qt.mouthY(-8);
  }

  purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -5, duration: 0.25, ease: 'power2.out', overwrite: 'auto' });
  });
  blackRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: -4, y: -4, duration: 0.25, ease: 'power2.out', overwrite: 'auto' });
  });
  orangeRef.value?.querySelectorAll('.pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -5, duration: 0.25, ease: 'power2.out', overwrite: 'auto' });
  });
  yellowRef.value?.querySelectorAll('.pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -5, duration: 0.25, ease: 'power2.out', overwrite: 'auto' });
  });
};

onMounted(() => {
  gsap.set('.pupil', { x: 0, y: 0 });
  gsap.set('.eyeball-pupil', { x: 0, y: 0 });

  if (
    !purpleRef.value ||
    !blackRef.value ||
    !orangeRef.value ||
    !yellowRef.value ||
    !purpleFaceRef.value ||
    !blackFaceRef.value ||
    !orangeFaceRef.value ||
    !yellowFaceRef.value ||
    !yellowMouthRef.value
  ) {
    return;
  }

  quickToRef.value = {
    purpleSkew: gsap.quickTo(purpleRef.value, 'skewX', { duration: 0.3, ease: 'power2.out' }),
    blackSkew: gsap.quickTo(blackRef.value, 'skewX', { duration: 0.3, ease: 'power2.out' }),
    orangeSkew: gsap.quickTo(orangeRef.value, 'skewX', { duration: 0.3, ease: 'power2.out' }),
    yellowSkew: gsap.quickTo(yellowRef.value, 'skewX', { duration: 0.3, ease: 'power2.out' }),
    purpleX: gsap.quickTo(purpleRef.value, 'x', { duration: 0.3, ease: 'power2.out' }),
    blackX: gsap.quickTo(blackRef.value, 'x', { duration: 0.3, ease: 'power2.out' }),
    purpleHeight: gsap.quickTo(purpleRef.value, 'height', { duration: 0.3, ease: 'power2.out' }),
    purpleFaceLeft: gsap.quickTo(purpleFaceRef.value, 'left', { duration: 0.3, ease: 'power2.out' }),
    purpleFaceTop: gsap.quickTo(purpleFaceRef.value, 'top', { duration: 0.3, ease: 'power2.out' }),
    blackFaceLeft: gsap.quickTo(blackFaceRef.value, 'left', { duration: 0.3, ease: 'power2.out' }),
    blackFaceTop: gsap.quickTo(blackFaceRef.value, 'top', { duration: 0.3, ease: 'power2.out' }),
    orangeFaceX: gsap.quickTo(orangeFaceRef.value, 'x', { duration: 0.2, ease: 'power2.out' }),
    orangeFaceY: gsap.quickTo(orangeFaceRef.value, 'y', { duration: 0.2, ease: 'power2.out' }),
    yellowFaceX: gsap.quickTo(yellowFaceRef.value, 'x', { duration: 0.2, ease: 'power2.out' }),
    yellowFaceY: gsap.quickTo(yellowFaceRef.value, 'y', { duration: 0.2, ease: 'power2.out' }),
    mouthX: gsap.quickTo(yellowMouthRef.value, 'x', { duration: 0.2, ease: 'power2.out' }),
    mouthY: gsap.quickTo(yellowMouthRef.value, 'y', { duration: 0.2, ease: 'power2.out' }),
  };

  const tick = () => {
    const container = containerRef.value;
    const qt = quickToRef.value;
    if (!container || !qt) return;

    const { isTyping, isHidingPassword, isShowingPassword, isLooking, isPasswordGuardMode } = stateRef.value;

    if (isPasswordGuardMode) {
      applyPasswordGuardMode();
      rafIdRef.value = requestAnimationFrame(tick);
      return;
    }

    if (purpleRef.value && !isShowingPassword) {
      const pp = calcPos(purpleRef.value);
      if (isTyping || isHidingPassword) {
        qt.purpleSkew(pp.bodySkew - 12);
        qt.purpleX(40);
        qt.purpleHeight(440);
      } else {
        qt.purpleSkew(pp.bodySkew);
        qt.purpleX(0);
        qt.purpleHeight(400);
      }
    }

    if (blackRef.value && !isShowingPassword) {
      const bp = calcPos(blackRef.value);
      if (isLooking) {
        qt.blackSkew(bp.bodySkew * 1.5 + 10);
        qt.blackX(20);
      } else if (isTyping || isHidingPassword) {
        qt.blackSkew(bp.bodySkew * 1.5);
        qt.blackX(0);
      } else {
        qt.blackSkew(bp.bodySkew);
        qt.blackX(0);
      }
    }

    if (orangeRef.value && !isShowingPassword) {
      const op = calcPos(orangeRef.value);
      qt.orangeSkew(op.bodySkew);
      qt.orangeFaceX(op.faceX);
      qt.orangeFaceY(op.faceY);
    }

    if (yellowRef.value && !isShowingPassword) {
      const yp = calcPos(yellowRef.value);
      qt.yellowSkew(yp.bodySkew);
      qt.yellowFaceX(yp.faceX);
      qt.yellowFaceY(yp.faceY);
      qt.mouthX(yp.faceX);
      qt.mouthY(yp.faceY);
    }

    if (purpleRef.value && !isShowingPassword && !isLooking) {
      const pp = calcPos(purpleRef.value);
      const purpleFaceX = pp.faceX >= 0 ? Math.min(25, pp.faceX * 1.5) : pp.faceX;
      qt.purpleFaceLeft(45 + purpleFaceX);
      qt.purpleFaceTop(40 + pp.faceY);
    }

    if (blackRef.value && !isShowingPassword && !isLooking) {
      const bp = calcPos(blackRef.value);
      qt.blackFaceLeft(26 + bp.faceX);
      qt.blackFaceTop(32 + bp.faceY);
    }

    if (!isShowingPassword) {
      container.querySelectorAll('.pupil').forEach((p) => {
        const el = p as HTMLElement;
        const maxDist = Number(el.dataset.maxDistance) || 5;
        const ePos = calcEyePos(el, maxDist);
        gsap.set(el, { x: ePos.x, y: ePos.y });
      });

      if (!isLooking) {
        container.querySelectorAll('.eyeball').forEach((eb) => {
          const el = eb as HTMLElement;
          const maxDist = Number(el.dataset.maxDistance) || 10;
          const pupil = el.querySelector('.eyeball-pupil') as HTMLElement | null;
          if (!pupil) return;
          const ePos = calcEyePos(el, maxDist);
          gsap.set(pupil, { x: ePos.x, y: ePos.y });
        });
      }
    }

    rafIdRef.value = requestAnimationFrame(tick);
  };

  const onMove = (e: MouseEvent) => {
    mouseRef.value = { x: e.clientX, y: e.clientY };
  };

  window.addEventListener('mousemove', onMove, { passive: true });
  rafIdRef.value = requestAnimationFrame(tick);

  const purpleEyeballs = purpleRef.value.querySelectorAll('.eyeball');
  const blackEyeballs = blackRef.value.querySelectorAll('.eyeball');

  const schedulePurpleBlink = () => {
    purpleBlinkTimerRef.value = setTimeout(() => {
      purpleEyeballs.forEach((el) => {
        gsap.to(el, { height: 2, duration: 0.08, ease: 'power2.in' });
      });
      setTimeout(() => {
        purpleEyeballs.forEach((el) => {
          const size = Number((el as HTMLElement).style.width.replace('px', '')) || 18;
          gsap.to(el, { height: size, duration: 0.08, ease: 'power2.out' });
        });
        schedulePurpleBlink();
      }, 150);
    }, Math.random() * 4000 + 3000);
  };

  const scheduleBlackBlink = () => {
    blackBlinkTimerRef.value = setTimeout(() => {
      blackEyeballs.forEach((el) => {
        gsap.to(el, { height: 2, duration: 0.08, ease: 'power2.in' });
      });
      setTimeout(() => {
        blackEyeballs.forEach((el) => {
          const size = Number((el as HTMLElement).style.width.replace('px', '')) || 16;
          gsap.to(el, { height: size, duration: 0.08, ease: 'power2.out' });
        });
        scheduleBlackBlink();
      }, 150);
    }, Math.random() * 4000 + 3000);
  };

  schedulePurpleBlink();
  scheduleBlackBlink();

  onBeforeUnmount(() => {
    window.removeEventListener('mousemove', onMove);
    cancelAnimationFrame(rafIdRef.value);
  });
});

watch(
  () => [props.isTyping, isHidingPassword.value, isShowingPassword.value, isLookingRef.value, props.isPasswordGuardMode],
  () => {
    stateRef.value = {
      isTyping: props.isTyping,
      isHidingPassword: isHidingPassword.value,
      isShowingPassword: isShowingPassword.value,
      isLooking: isLookingRef.value,
      isPasswordGuardMode: props.isPasswordGuardMode,
    };
  },
  { immediate: true },
);

watch(
  () => [isShowingPassword.value, props.passwordLength],
  () => {
    if (props.isPasswordGuardMode || !isShowingPassword.value || props.passwordLength <= 0) {
      clearTimeout(purplePeekTimerRef.value);
      return;
    }

    const purpleEyePupils = purpleRef.value?.querySelectorAll('.eyeball-pupil');
    if (!purpleEyePupils?.length) return;

    const schedulePeek = () => {
      purplePeekTimerRef.value = setTimeout(() => {
        purpleEyePupils.forEach((p) => {
          gsap.to(p, { x: 4, y: 5, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
        });
        const qt = quickToRef.value;
        if (qt) {
          qt.purpleFaceLeft(20);
          qt.purpleFaceTop(35);
        }

        setTimeout(() => {
          purpleEyePupils.forEach((p) => {
            gsap.to(p, { x: -4, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' });
          });
          schedulePeek();
        }, 800);
      }, Math.random() * 3000 + 2000);
    };

    schedulePeek();
  },
  { immediate: true },
);

watch(
  () => [props.isTyping, isShowingPassword.value],
  () => {
    if (props.isPasswordGuardMode) {
      clearTimeout(lookingTimerRef.value);
      isLookingRef.value = false;
      stateRef.value.isLooking = false;
      return;
    }

    if (props.isTyping && !isShowingPassword.value) {
      isLookingRef.value = true;
      stateRef.value.isLooking = true;
      applyLookAtEachOther();

      clearTimeout(lookingTimerRef.value);
      lookingTimerRef.value = setTimeout(() => {
        isLookingRef.value = false;
        stateRef.value.isLooking = false;
        purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
          gsap.killTweensOf(p);
        });
      }, 800);
    } else {
      clearTimeout(lookingTimerRef.value);
      isLookingRef.value = false;
      stateRef.value.isLooking = false;
    }
  },
  { immediate: true },
);

watch(
  () => [isHidingPassword.value, isShowingPassword.value],
  () => {
    if (props.isPasswordGuardMode) {
      applyPasswordGuardMode();
    } else if (isShowingPassword.value) {
      applyShowPassword();
    } else if (isHidingPassword.value) {
      applyHidingPassword();
    }
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  clearTimeout(purpleBlinkTimerRef.value);
  clearTimeout(blackBlinkTimerRef.value);
  clearTimeout(purplePeekTimerRef.value);
  clearTimeout(lookingTimerRef.value);
  cancelAnimationFrame(rafIdRef.value);
});
</script>
