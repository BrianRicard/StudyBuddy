import { useState, useRef, useCallback } from "react";

const C = {
  bg: "#FEFBF4", bgSoft: "#FFF8EC", card: "#FFFFFF",
  primary: "#FF6B4A", primarySoft: "#FFF0EC",
  secondary: "#4A90D9", secondarySoft: "#EBF3FC",
  accent: "#FFB84D", accentSoft: "#FFF4E0",
  green: "#34C759", greenSoft: "#E8FAE8",
  purple: "#9B6FE3", purpleSoft: "#F3EDFC",
  pink: "#FF6B9D", pinkSoft: "#FFEBF2",
  teal: "#2AC3A2", tealSoft: "#E6F9F4",
  text: "#1A1A2E", textSoft: "#6B7280", textMuted: "#9CA3AF",
  border: "#F0EBE3", shadow: "rgba(26,26,46,0.06)",
  danger: "#E53E3E", dangerSoft: "#FFF0F0",
};

const Phone = ({ children, label }) => (
  <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
    <div style={{ width: 360, height: 740, borderRadius: 44, background: "#1A1A2E", padding: 10, boxShadow: "0 25px 70px rgba(0,0,0,0.3), 0 0 0 1px rgba(255,255,255,0.08) inset", position: "relative" }}>
      <div style={{ position: "absolute", top: 10, left: "50%", transform: "translateX(-50%)", width: 110, height: 30, background: "#1A1A2E", borderRadius: "0 0 18px 18px", zIndex: 10 }}>
        <div style={{ width: 8, height: 8, borderRadius: "50%", background: "#2A2A3E", margin: "12px auto 0" }} />
      </div>
      <div style={{ width: "100%", height: "100%", borderRadius: 34, overflow: "hidden", background: C.bg, position: "relative" }}>{children}</div>
    </div>
    {label && <span style={{ fontFamily: "'DM Sans',sans-serif", fontSize: 13, color: "rgba(255,255,255,0.5)", fontWeight: 600 }}>{label}</span>}
  </div>
);

const StatusBar = () => (
  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "14px 20px 4px", fontSize: 12, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>
    <span>9:41</span>
    <div style={{ display: "flex", gap: 5, alignItems: "center" }}>
      <svg width="16" height="10" viewBox="0 0 16 10"><rect x="0" y="3" width="3" height="7" rx=".5" fill={C.text}/><rect x="4.5" y="2" width="3" height="8" rx=".5" fill={C.text}/><rect x="9" y="0" width="3" height="10" rx=".5" fill={C.text}/></svg>
      <div style={{ width: 22, height: 11, border: `1.5px solid ${C.text}`, borderRadius: 3, position: "relative" }}>
        <div style={{ position: "absolute", right: -4, top: 3, width: 2, height: 5, background: C.text, borderRadius: "0 1px 1px 0" }} />
        <div style={{ width: "70%", height: "100%", background: C.green, borderRadius: 1.5 }} />
      </div>
    </div>
  </div>
);

const NavBar = ({ active, onNav }) => (
  <div style={{ display: "flex", justifyContent: "space-around", alignItems: "center", padding: "6px 8px 22px", background: C.card, borderTop: `1px solid ${C.border}` }}>
    {[{ id: "home", icon: "🏠", l: "Home" }, { id: "stats", icon: "📊", l: "Stats" }, { id: "rewards", icon: "🎁", l: "Rewards" }, { id: "avatar-closet", icon: "👤", l: "Avatar" }, { id: "settings", icon: "⚙️", l: "Settings" }].map(i => (
      <button key={i.id} onClick={() => onNav(i.id)} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 1, background: "none", border: "none", cursor: "pointer", padding: "4px 8px", opacity: active === i.id ? 1 : 0.35, transform: active === i.id ? "scale(1.08)" : "scale(1)", transition: "all 0.2s" }}>
        <span style={{ fontSize: 19 }}>{i.icon}</span>
        <span style={{ fontSize: 9, fontWeight: 700, color: active === i.id ? C.primary : C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>{i.l}</span>
      </button>
    ))}
  </div>
);

const Header = ({ title, subtitle, right, onBack }) => (
  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "6px 20px 10px" }}>
    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
      {onBack && <button onClick={onBack} style={{ background: "none", border: "none", cursor: "pointer", fontSize: 20, padding: 4, color: C.text }}>←</button>}
      <div>
        <div style={{ fontSize: 20, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif", letterSpacing: -0.5 }}>{title}</div>
        {subtitle && <div style={{ fontSize: 11, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: -2 }}>{subtitle}</div>}
      </div>
    </div>
    {right}
  </div>
);

const Toggle = ({ on }) => (
  <div style={{ width: 44, height: 24, borderRadius: 12, background: on ? C.green : C.border, padding: 2, cursor: "pointer" }}>
    <div style={{ width: 20, height: 20, borderRadius: 10, background: "#FFF", boxShadow: `0 1px 3px ${C.shadow}`, marginLeft: on ? 20 : 0, transition: "margin 0.2s" }} />
  </div>
);

const PointsBadge = ({ pts = "1,240" }) => (
  <div style={{ display: "flex", alignItems: "center", gap: 4, background: C.accentSoft, padding: "6px 12px", borderRadius: 16 }}>
    <span style={{ fontSize: 13 }}>⭐</span>
    <span style={{ fontSize: 14, fontWeight: 800, color: C.accent, fontFamily: "'Nunito',sans-serif" }}>{pts}</span>
  </div>
);

/* ===================== AVATAR SYSTEM ===================== */
const AVATAR_BODIES = [
  { id: "fox", emoji: "🦊", color: "#FF8C42" },
  { id: "cat", emoji: "🐱", color: "#FFB347" },
  { id: "unicorn", emoji: "🦄", color: "#C77DFF" },
  { id: "panda", emoji: "🐼", color: "#6B7280" },
  { id: "butterfly", emoji: "🦋", color: "#4A90D9" },
  { id: "bunny", emoji: "🐰", color: "#FF6B9D" },
  { id: "owl", emoji: "🦉", color: "#8B5E3C" },
  { id: "dragon", emoji: "🐉", color: "#34C759" },
];

const ACCESSORIES = {
  hats: [
    { id: "none", label: "None", icon: "—", cost: 0, owned: true },
    { id: "tophat", label: "Top Hat", icon: "🎩", cost: 0, owned: true },
    { id: "crown", label: "Crown", icon: "👑", cost: 250, owned: false },
    { id: "wizard", label: "Wizard", icon: "🧙", cost: 400, owned: false },
    { id: "party", label: "Party", icon: "🥳", cost: 150, owned: true },
    { id: "beret", label: "Beret", icon: "🫐", cost: 200, owned: false },
    { id: "flower", label: "Flower", icon: "🌺", cost: 100, owned: true },
    { id: "cap", label: "Cap", icon: "🧢", cost: 150, owned: false },
  ],
  faces: [
    { id: "none", label: "None", icon: "—", cost: 0, owned: true },
    { id: "shades", label: "Shades", icon: "🕶️", cost: 0, owned: true },
    { id: "monocle", label: "Monocle", icon: "🧐", cost: 200, owned: false },
    { id: "glasses", label: "Glasses", icon: "👓", cost: 100, owned: true },
    { id: "mask", label: "Mask", icon: "🎭", cost: 300, owned: false },
    { id: "star", label: "Star", icon: "⭐", cost: 350, owned: false },
  ],
  outfits: [
    { id: "none", label: "None", icon: "—", cost: 0, owned: true },
    { id: "scarf", label: "Scarf", icon: "🧣", cost: 200, owned: true },
    { id: "bow", label: "Bow Tie", icon: "🎀", cost: 150, owned: false },
    { id: "cape", label: "Cape", icon: "🦸", cost: 500, owned: false },
    { id: "medal", label: "Medal", icon: "🏅", cost: 300, owned: false },
    { id: "necklace", label: "Necklace", icon: "📿", cost: 250, owned: false },
  ],
  pets: [
    { id: "none", label: "None", icon: "—", cost: 0, owned: true },
    { id: "chick", label: "Chick", icon: "🐥", cost: 300, owned: true },
    { id: "hamster", label: "Hamster", icon: "🐹", cost: 400, owned: false },
    { id: "fish", label: "Fish", icon: "🐠", cost: 250, owned: false },
    { id: "snail", label: "Snail", icon: "🐌", cost: 200, owned: false },
    { id: "ladybug", label: "Ladybug", icon: "🐞", cost: 150, owned: false },
  ],
};

const AvatarDisplay = ({ body, hat, face, outfit, pet, size = 100, bg = true }) => {
  const bodyData = AVATAR_BODIES.find(b => b.id === body) || AVATAR_BODIES[2];
  const hatData = ACCESSORIES.hats.find(h => h.id === hat);
  const faceData = ACCESSORIES.faces.find(f => f.id === face);
  const outfitData = ACCESSORIES.outfits.find(o => o.id === outfit);
  const petData = ACCESSORIES.pets.find(p => p.id === pet);
  const fs = size * 0.45;
  const accFs = size * 0.22;
  return (
    <div style={{ width: size, height: size, borderRadius: size * 0.28, background: bg ? `${bodyData.color}18` : "transparent", display: "flex", alignItems: "center", justifyContent: "center", position: "relative", flexShrink: 0 }}>
      <span style={{ fontSize: fs }}>{bodyData.emoji}</span>
      {hatData && hatData.id !== "none" && <span style={{ position: "absolute", top: size * 0.02, left: "50%", transform: "translateX(-50%)", fontSize: accFs }}>{hatData.icon}</span>}
      {faceData && faceData.id !== "none" && <span style={{ position: "absolute", top: size * 0.55, right: size * 0.08, fontSize: accFs * 0.8 }}>{faceData.icon}</span>}
      {outfitData && outfitData.id !== "none" && <span style={{ position: "absolute", bottom: size * 0.04, left: "50%", transform: "translateX(-50%)", fontSize: accFs * 0.85 }}>{outfitData.icon}</span>}
      {petData && petData.id !== "none" && <span style={{ position: "absolute", bottom: size * -0.05, right: size * -0.05, fontSize: accFs * 0.9 }}>{petData.icon}</span>}
    </div>
  );
};

/* ===================== ONBOARDING ===================== */
const OnboardingScreen = () => {
  const [step, setStep] = useState(0);
  const [selectedBody, setSelectedBody] = useState("unicorn");
  const [selectedHat, setSelectedHat] = useState("none");
  const [selectedFace, setSelectedFace] = useState("none");

  if (step === 0) return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
      <StatusBar />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "0 28px", gap: 18 }}>
        <div style={{ width: 90, height: 90, borderRadius: 24, background: C.accentSoft, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 46 }}>👋</div>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontSize: 24, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif" }}>Welcome!</div>
          <div style={{ fontSize: 13, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 4, lineHeight: 1.5 }}>Let's set up your profile and create your buddy.</div>
        </div>
        <div style={{ width: "100%", marginTop: 8 }}>
          <label style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", display: "block", marginBottom: 6 }}>What's your name?</label>
          <input placeholder="Enter your name..." defaultValue="Léa" readOnly style={{ width: "100%", padding: "14px 16px", borderRadius: 14, border: `2px solid ${C.primary}`, fontSize: 16, fontFamily: "'DM Sans',sans-serif", color: C.text, background: C.card, outline: "none", boxSizing: "border-box" }} />
        </div>
        <div style={{ width: "100%" }}>
          <label style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", display: "block", marginBottom: 8 }}>App language</label>
          <div style={{ display: "flex", gap: 8 }}>
            {[{ f: "🇫🇷", l: "Français" }, { f: "🇬🇧", l: "English" }, { f: "🇩🇪", l: "Deutsch" }].map((lang, i) => (
              <div key={i} style={{ flex: 1, padding: "10px 6px", borderRadius: 12, textAlign: "center", fontSize: 11, fontWeight: 600, fontFamily: "'DM Sans',sans-serif", cursor: "pointer", background: i === 0 ? C.secondarySoft : C.bgSoft, border: i === 0 ? `2px solid ${C.secondary}` : `2px solid ${C.border}`, color: i === 0 ? C.secondary : C.textSoft }}>
                <span style={{ fontSize: 18, display: "block", marginBottom: 2 }}>{lang.f}</span>{lang.l}
              </div>
            ))}
          </div>
        </div>
      </div>
      <div style={{ padding: "12px 28px 32px" }}>
        <div onClick={() => setStep(1)} style={{ padding: "16px", borderRadius: 16, background: C.primary, textAlign: "center", color: "#FFF", fontWeight: 800, fontSize: 16, fontFamily: "'Nunito',sans-serif", cursor: "pointer" }}>Next → Choose Your Buddy</div>
      </div>
    </div>
  );

  if (step === 1) return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
      <StatusBar />
      <div style={{ flex: 1, overflowY: "auto", padding: "0 24px" }}>
        <div style={{ textAlign: "center", marginTop: 12, marginBottom: 16 }}>
          <div style={{ fontSize: 20, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif" }}>Choose Your Buddy</div>
          <div style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 2 }}>Pick a character and dress them up!</div>
        </div>

        {/* Avatar Preview */}
        <div style={{ display: "flex", justifyContent: "center", marginBottom: 16 }}>
          <AvatarDisplay body={selectedBody} hat={selectedHat} face={selectedFace} outfit="none" pet="none" size={120} />
        </div>

        {/* Body Selection */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8 }}>Character</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 8 }}>
            {AVATAR_BODIES.map(b => (
              <div key={b.id} onClick={() => setSelectedBody(b.id)} style={{ height: 56, borderRadius: 14, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 26, cursor: "pointer", background: selectedBody === b.id ? `${b.color}20` : C.bgSoft, border: selectedBody === b.id ? `2.5px solid ${b.color}` : `2px solid ${C.border}`, transition: "all 0.2s" }}>
                {b.emoji}
              </div>
            ))}
          </div>
        </div>

        {/* Starter Accessories */}
        <div style={{ marginBottom: 12 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8 }}>Starter Hat <span style={{ fontWeight: 400, color: C.textMuted }}>(earn more in shop!)</span></div>
          <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
            {ACCESSORIES.hats.filter(h => h.owned).map(h => (
              <div key={h.id} onClick={() => setSelectedHat(h.id)} style={{ width: 52, height: 52, borderRadius: 14, display: "flex", alignItems: "center", justifyContent: "center", fontSize: h.id === "none" ? 14 : 22, cursor: "pointer", background: selectedHat === h.id ? C.primarySoft : C.bgSoft, border: selectedHat === h.id ? `2.5px solid ${C.primary}` : `2px solid ${C.border}`, color: h.id === "none" ? C.textMuted : C.text }}>
                {h.icon}
              </div>
            ))}
          </div>
        </div>
        <div style={{ marginBottom: 12 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8 }}>Starter Face Accessory</div>
          <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
            {ACCESSORIES.faces.filter(f => f.owned).map(f => (
              <div key={f.id} onClick={() => setSelectedFace(f.id)} style={{ width: 52, height: 52, borderRadius: 14, display: "flex", alignItems: "center", justifyContent: "center", fontSize: f.id === "none" ? 14 : 22, cursor: "pointer", background: selectedFace === f.id ? C.primarySoft : C.bgSoft, border: selectedFace === f.id ? `2.5px solid ${C.primary}` : `2px solid ${C.border}`, color: f.id === "none" ? C.textMuted : C.text }}>
                {f.icon}
              </div>
            ))}
          </div>
        </div>

        <div style={{ padding: "10px 14px", borderRadius: 14, background: C.accentSoft, fontSize: 11, color: C.text, fontFamily: "'DM Sans',sans-serif", lineHeight: 1.5, textAlign: "center" }}>
          🎁 Earn ⭐ stars by studying to unlock more outfits, pets, and accessories in the Rewards Shop!
        </div>
      </div>
      <div style={{ padding: "10px 24px 28px", display: "flex", gap: 10 }}>
        <div onClick={() => setStep(0)} style={{ flex: 1, padding: "14px", borderRadius: 16, background: C.bgSoft, textAlign: "center", color: C.textSoft, fontWeight: 700, fontSize: 14, fontFamily: "'Nunito',sans-serif", cursor: "pointer", border: `1.5px solid ${C.border}` }}>← Back</div>
        <div onClick={() => setStep(2)} style={{ flex: 2, padding: "14px", borderRadius: 16, background: C.primary, textAlign: "center", color: "#FFF", fontWeight: 800, fontSize: 14, fontFamily: "'Nunito',sans-serif", cursor: "pointer" }}>Next → Voice Setup</div>
      </div>
    </div>
  );

  /* Step 2: TTS download */
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
      <StatusBar />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", padding: "0 24px" }}>
        <div style={{ textAlign: "center", marginTop: 16, marginBottom: 20 }}>
          <div style={{ fontSize: 40, marginBottom: 8 }}>🔊</div>
          <div style={{ fontSize: 20, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif" }}>Download Voices</div>
          <div style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 4, lineHeight: 1.5 }}>StudyBuddy needs voice packs to read words aloud — even without internet!</div>
        </div>

        {[
          { flag: "🇫🇷", lang: "French", size: "28 MB", status: "done" },
          { flag: "🇬🇧", lang: "English", size: "24 MB", status: "downloading", pct: 65 },
          { flag: "🇩🇪", lang: "German", size: "26 MB", status: "waiting" },
        ].map((v, i) => (
          <div key={i} style={{ padding: "14px 16px", borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 10 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: v.status === "downloading" ? 10 : 0 }}>
              <span style={{ fontSize: 24 }}>{v.flag}</span>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{v.lang}</div>
                <div style={{ fontSize: 11, color: C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>High-quality voice · {v.size}</div>
              </div>
              {v.status === "done" && <div style={{ padding: "6px 12px", borderRadius: 10, background: C.greenSoft, fontSize: 11, fontWeight: 700, color: C.green, fontFamily: "'DM Sans',sans-serif" }}>✓ Ready</div>}
              {v.status === "waiting" && <div style={{ padding: "6px 12px", borderRadius: 10, background: C.bgSoft, fontSize: 11, fontWeight: 700, color: C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>Waiting...</div>}
              {v.status === "downloading" && <div style={{ fontSize: 12, fontWeight: 700, color: C.secondary, fontFamily: "'DM Sans',sans-serif" }}>{v.pct}%</div>}
            </div>
            {v.status === "downloading" && (
              <div style={{ height: 6, borderRadius: 3, background: C.secondarySoft, overflow: "hidden" }}>
                <div style={{ width: `${v.pct}%`, height: "100%", borderRadius: 3, background: C.secondary, transition: "width 0.5s" }} />
              </div>
            )}
          </div>
        ))}

        <div style={{ padding: "12px 14px", borderRadius: 14, background: C.bgSoft, fontSize: 11, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", lineHeight: 1.5, marginTop: 8 }}>
          💡 Voices are stored on your device so dictée works offline. You can manage voice packs later in Settings.
        </div>

        <div style={{ flex: 1 }} />
      </div>
      <div style={{ padding: "10px 24px 28px" }}>
        <div style={{ padding: "16px", borderRadius: 16, background: C.primary, textAlign: "center", color: "#FFF", fontWeight: 800, fontSize: 16, fontFamily: "'Nunito',sans-serif", cursor: "pointer" }}>Let's Go! 🚀</div>
        <div style={{ textAlign: "center", marginTop: 8, fontSize: 12, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", cursor: "pointer" }}>Skip for now →</div>
      </div>
    </div>
  );
};

/* ===================== HOME SCREEN ===================== */
const HomeScreen = ({ onNav }) => (
  <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
    <StatusBar />
    <div style={{ flex: 1, overflowY: "auto" }}>
      <div style={{ padding: "6px 20px 14px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
          <div onClick={() => onNav("avatar-closet")} style={{ cursor: "pointer" }}>
            <AvatarDisplay body="unicorn" hat="party" face="shades" outfit="scarf" pet="chick" size={50} />
          </div>
          <div>
            <div style={{ fontSize: 11, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>Bonjour!</div>
            <div style={{ fontSize: 20, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif", marginTop: -2 }}>Léa</div>
          </div>
        </div>
        <PointsBadge />
      </div>

      <div style={{ margin: "0 20px 14px", padding: "12px 16px", borderRadius: 16, background: "linear-gradient(135deg, #FF6B4A18, #FFB84D18)", border: `1.5px solid ${C.primary}18`, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <div style={{ fontSize: 13, fontWeight: 800, color: C.primary, fontFamily: "'Nunito',sans-serif" }}>🔥 3-day streak!</div>
          <div style={{ fontSize: 10, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 1 }}>4 more days → bonus 100 ⭐</div>
        </div>
        <div style={{ display: "flex", gap: 3 }}>{[1,1,1,0,0,0,0].map((f,i)=><div key={i} style={{width:7,height:7,borderRadius:4,background:f?C.primary:C.border}}/>)}</div>
      </div>

      <div style={{ margin: "0 20px 16px", padding: "12px 16px", borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, boxShadow: `0 2px 10px ${C.shadow}` }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
          <span style={{ fontSize: 12, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>🎯 Daily Challenge</span>
          <span style={{ fontSize: 11, color: C.green, fontWeight: 700, fontFamily: "'DM Sans',sans-serif" }}>2/5</span>
        </div>
        <div style={{ height: 7, borderRadius: 4, background: C.greenSoft, overflow: "hidden" }}><div style={{ width: "40%", height: "100%", borderRadius: 4, background: C.green }} /></div>
      </div>

      <div style={{ padding: "0 20px" }}>
        <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 10, textTransform: "uppercase", letterSpacing: 1 }}>Study Modes</div>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10 }}>
          {[
            { e: "📝", t: "Dictée", d: "Practice spelling", bg: `${C.primary}12`, bc: C.primary, c: C.primary },
            { e: "⚡", t: "Speed Math", d: "Quick calculations", bg: `${C.secondary}12`, bc: C.secondary, c: C.secondary },
            { e: "📖", t: "Poems", d: "Coming soon!", bg: `${C.purple}12`, bc: C.purple, c: C.purple, lock: true },
            { e: "🧩", t: "More", d: "Coming soon!", bg: `${C.pink}12`, bc: C.pink, c: C.pink, lock: true },
          ].map((m, i) => (
            <div key={i} style={{ padding: "16px 14px", borderRadius: 18, background: m.bg, border: `1.5px solid ${m.bc}20`, cursor: m.lock ? "default" : "pointer", opacity: m.lock ? 0.5 : 1, position: "relative" }}>
              {m.lock && <div style={{ position: "absolute", top: 8, right: 8, fontSize: 10, background: C.bgSoft, borderRadius: 6, padding: "2px 6px", color: C.textMuted, fontWeight: 600, fontFamily: "'DM Sans',sans-serif" }}>🔒</div>}
              <div style={{ fontSize: 28, marginBottom: 6 }}>{m.e}</div>
              <div style={{ fontSize: 15, fontWeight: 800, color: m.c, fontFamily: "'Nunito',sans-serif" }}>{m.t}</div>
              <div style={{ fontSize: 10, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 1 }}>{m.d}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ padding: "16px 20px 8px" }}>
        <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 1 }}>Recent</div>
        {[
          { icon: "⚡", text: "Speed Math: 18/20", time: "2h ago", pts: "+95" },
          { icon: "📝", text: "Dictée: Liste du lundi", time: "Yesterday", pts: "+60" },
        ].map((a, i) => (
          <div key={i} style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 0", borderBottom: i === 0 ? `1px solid ${C.border}` : "none" }}>
            <div style={{ width: 34, height: 34, borderRadius: 10, background: C.bgSoft, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16 }}>{a.icon}</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 12, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{a.text}</div>
              <div style={{ fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>{a.time}</div>
            </div>
            <span style={{ fontSize: 12, fontWeight: 700, color: C.green, fontFamily: "'DM Sans',sans-serif" }}>{a.pts}</span>
          </div>
        ))}
      </div>
    </div>
    <NavBar active="home" onNav={onNav} />
  </div>
);

/* ===================== AVATAR CLOSET ===================== */
const AvatarCloset = ({ onNav }) => {
  const [tab, setTab] = useState("hats");
  const tabs = [
    { id: "hats", label: "Hats", icon: "🎩" },
    { id: "faces", label: "Face", icon: "🕶️" },
    { id: "outfits", label: "Outfit", icon: "👔" },
    { id: "pets", label: "Pets", icon: "🐾" },
  ];
  const items = ACCESSORIES[tab] || [];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
      <StatusBar />
      <Header title="My Avatar" subtitle="Dress up your buddy!" right={<PointsBadge />} />
      <div style={{ flex: 1, overflowY: "auto", padding: "0 20px" }}>
        {/* Avatar preview */}
        <div style={{ display: "flex", justifyContent: "center", padding: "8px 0 16px" }}>
          <div style={{ position: "relative" }}>
            <AvatarDisplay body="unicorn" hat="party" face="shades" outfit="scarf" pet="chick" size={130} />
            <div style={{ position: "absolute", bottom: -4, right: -4, width: 32, height: 32, borderRadius: 10, background: C.primary, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 14, cursor: "pointer", boxShadow: `0 2px 8px ${C.primary}44` }}>✏️</div>
          </div>
        </div>

        {/* Character swap */}
        <div style={{ marginBottom: 14 }}>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Character</div>
          <div style={{ display: "flex", gap: 6, overflowX: "auto", paddingBottom: 4 }}>
            {AVATAR_BODIES.map(b => (
              <div key={b.id} style={{ width: 48, height: 48, borderRadius: 14, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 22, flexShrink: 0, cursor: "pointer", background: b.id === "unicorn" ? `${b.color}25` : C.bgSoft, border: b.id === "unicorn" ? `2.5px solid ${b.color}` : `2px solid ${C.border}` }}>
                {b.emoji}
              </div>
            ))}
          </div>
        </div>

        {/* Accessory tabs */}
        <div style={{ display: "flex", gap: 4, marginBottom: 14, background: C.bgSoft, borderRadius: 12, padding: 3 }}>
          {tabs.map(t => (
            <div key={t.id} onClick={() => setTab(t.id)} style={{ flex: 1, padding: "8px 4px", borderRadius: 10, textAlign: "center", cursor: "pointer", background: tab === t.id ? C.card : "transparent", boxShadow: tab === t.id ? `0 1px 4px ${C.shadow}` : "none", transition: "all 0.2s" }}>
              <div style={{ fontSize: 16 }}>{t.icon}</div>
              <div style={{ fontSize: 9, fontWeight: 700, color: tab === t.id ? C.text : C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>{t.label}</div>
            </div>
          ))}
        </div>

        {/* Items grid */}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8, paddingBottom: 12 }}>
          {items.map(item => (
            <div key={item.id} style={{ padding: "12px 8px", borderRadius: 16, background: item.owned ? C.card : C.bgSoft, border: `1.5px solid ${item.owned ? C.border : C.border}`, textAlign: "center", cursor: "pointer", opacity: item.owned ? 1 : 0.65, position: "relative" }}>
              {!item.owned && <div style={{ position: "absolute", top: 6, right: 6, fontSize: 8, background: C.accentSoft, borderRadius: 6, padding: "2px 5px", fontWeight: 700, color: C.accent, fontFamily: "'DM Sans',sans-serif" }}>⭐{item.cost}</div>}
              <div style={{ fontSize: item.id === "none" ? 16 : 28, color: item.id === "none" ? C.textMuted : C.text, marginBottom: 2 }}>{item.icon}</div>
              <div style={{ fontSize: 10, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{item.label}</div>
              {item.owned && item.id !== "none" && <div style={{ fontSize: 9, color: C.green, fontWeight: 700, fontFamily: "'DM Sans',sans-serif", marginTop: 2 }}>Owned ✓</div>}
              {!item.owned && <div style={{ fontSize: 9, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", marginTop: 2 }}>Locked</div>}
            </div>
          ))}
        </div>
      </div>
      <NavBar active="avatar-closet" onNav={onNav} />
    </div>
  );
};

/* ===================== EXPANDED REWARDS SHOP ===================== */
const RewardsScreen = ({ onNav }) => {
  const [tab, setTab] = useState("avatar");
  const shopTabs = [
    { id: "avatar", label: "Avatar", icon: "👤" },
    { id: "themes", label: "Themes", icon: "🎨" },
    { id: "effects", label: "Effects", icon: "✨" },
    { id: "titles", label: "Titles", icon: "🏷️" },
  ];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
      <StatusBar />
      <Header title="Rewards Shop" subtitle="Spend your stars!" right={<PointsBadge />} />
      <div style={{ flex: 1, overflowY: "auto", padding: "0 20px" }}>
        {/* Shop tabs */}
        <div style={{ display: "flex", gap: 4, marginBottom: 14, background: C.bgSoft, borderRadius: 12, padding: 3 }}>
          {shopTabs.map(t => (
            <div key={t.id} onClick={() => setTab(t.id)} style={{ flex: 1, padding: "8px 4px", borderRadius: 10, textAlign: "center", cursor: "pointer", background: tab === t.id ? C.card : "transparent", boxShadow: tab === t.id ? `0 1px 4px ${C.shadow}` : "none" }}>
              <div style={{ fontSize: 14 }}>{t.icon}</div>
              <div style={{ fontSize: 9, fontWeight: 700, color: tab === t.id ? C.text : C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>{t.label}</div>
            </div>
          ))}
        </div>

        {tab === "avatar" && <>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Hats</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8, marginBottom: 16 }}>
            {ACCESSORIES.hats.filter(h => h.id !== "none").map(item => (
              <div key={item.id} style={{ padding: "14px 8px", borderRadius: 16, background: item.owned ? C.greenSoft : C.card, border: `1.5px solid ${item.owned ? C.green + "33" : C.border}`, textAlign: "center" }}>
                <div style={{ fontSize: 28, marginBottom: 4 }}>{item.icon}</div>
                <div style={{ fontSize: 10, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{item.label}</div>
                {item.owned ? <div style={{ marginTop: 4, fontSize: 10, color: C.green, fontWeight: 700 }}>✓ Owned</div> : <div style={{ marginTop: 4, padding: "4px 10px", borderRadius: 8, background: C.accentSoft, fontSize: 10, fontWeight: 700, color: C.accent, fontFamily: "'DM Sans',sans-serif", display: "inline-block", cursor: "pointer" }}>⭐ {item.cost}</div>}
              </div>
            ))}
          </div>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Pets</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8, marginBottom: 16 }}>
            {ACCESSORIES.pets.filter(p => p.id !== "none").map(item => (
              <div key={item.id} style={{ padding: "14px 8px", borderRadius: 16, background: item.owned ? C.greenSoft : C.card, border: `1.5px solid ${item.owned ? C.green + "33" : C.border}`, textAlign: "center" }}>
                <div style={{ fontSize: 28, marginBottom: 4 }}>{item.icon}</div>
                <div style={{ fontSize: 10, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{item.label}</div>
                {item.owned ? <div style={{ marginTop: 4, fontSize: 10, color: C.green, fontWeight: 700 }}>✓ Owned</div> : <div style={{ marginTop: 4, padding: "4px 10px", borderRadius: 8, background: C.accentSoft, fontSize: 10, fontWeight: 700, color: C.accent, fontFamily: "'DM Sans',sans-serif", display: "inline-block", cursor: "pointer" }}>⭐ {item.cost}</div>}
              </div>
            ))}
          </div>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Outfits & Face</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 8, marginBottom: 16 }}>
            {[...ACCESSORIES.outfits, ...ACCESSORIES.faces].filter(o => o.id !== "none").map(item => (
              <div key={item.id} style={{ padding: "14px 8px", borderRadius: 16, background: item.owned ? C.greenSoft : C.card, border: `1.5px solid ${item.owned ? C.green + "33" : C.border}`, textAlign: "center" }}>
                <div style={{ fontSize: 28, marginBottom: 4 }}>{item.icon}</div>
                <div style={{ fontSize: 10, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{item.label}</div>
                {item.owned ? <div style={{ marginTop: 4, fontSize: 10, color: C.green, fontWeight: 700 }}>✓ Owned</div> : <div style={{ marginTop: 4, padding: "4px 10px", borderRadius: 8, background: C.accentSoft, fontSize: 10, fontWeight: 700, color: C.accent, fontFamily: "'DM Sans',sans-serif", display: "inline-block", cursor: "pointer" }}>⭐ {item.cost}</div>}
              </div>
            ))}
          </div>
        </>}

        {tab === "themes" && <>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 10, textTransform: "uppercase", letterSpacing: 0.8 }}>App Themes</div>
          {[
            { name: "Sunset", colors: ["#FF6B4A", "#FF8E6B", "#FFB84D"], cost: 300, owned: true },
            { name: "Ocean", colors: ["#0077B6", "#00B4D8", "#90E0EF"], cost: 300, owned: false },
            { name: "Forest", colors: ["#2D6A4F", "#52B788", "#B7E4C7"], cost: 300, owned: false },
            { name: "Galaxy", colors: ["#5A189A", "#9D4EDD", "#E0AAFF"], cost: 500, owned: false },
            { name: "Candy", colors: ["#FF6B9D", "#FFB3D9", "#FFF0F5"], cost: 400, owned: false },
            { name: "Arctic", colors: ["#A8DADC", "#457B9D", "#1D3557"], cost: 500, owned: false },
          ].map((t, i) => (
            <div key={i} style={{ borderRadius: 16, overflow: "hidden", border: `1.5px solid ${t.owned ? C.green + "44" : C.border}`, marginBottom: 10 }}>
              <div style={{ height: 56, background: `linear-gradient(135deg, ${t.colors.join(", ")})`, position: "relative" }}>
                {t.owned && <div style={{ position: "absolute", top: 8, right: 8, padding: "4px 10px", borderRadius: 8, background: "rgba(255,255,255,0.9)", fontSize: 10, fontWeight: 700, color: C.green }}>✓ Active</div>}
              </div>
              <div style={{ padding: "10px 14px", background: C.card, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <span style={{ fontSize: 13, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{t.name}</span>
                {t.owned ? <span style={{ fontSize: 11, color: C.green, fontWeight: 700 }}>Owned</span> : <div style={{ padding: "4px 12px", borderRadius: 10, background: C.accentSoft, fontSize: 11, fontWeight: 700, color: C.accent, cursor: "pointer" }}>⭐ {t.cost}</div>}
              </div>
            </div>
          ))}
        </>}

        {tab === "effects" && <>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 10, textTransform: "uppercase", letterSpacing: 0.8 }}>Celebration Effects</div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 16 }}>
            {[
              { icon: "🎉", name: "Confetti", desc: "Classic celebration", cost: 0, owned: true },
              { icon: "🎆", name: "Fireworks", desc: "Light up the sky", cost: 400, owned: false },
              { icon: "🦄", name: "Unicorn Dance", desc: "Magical sparkles", cost: 600, owned: false },
              { icon: "🌈", name: "Rainbow Burst", desc: "Colors everywhere", cost: 500, owned: false },
              { icon: "⭐", name: "Star Shower", desc: "Golden star rain", cost: 350, owned: false },
              { icon: "🎸", name: "Rock Star", desc: "Guitar riff + lights", cost: 750, owned: false },
              { icon: "🏆", name: "Champion", desc: "Trophy celebration", cost: 500, owned: false },
              { icon: "🐉", name: "Dragon Fire", desc: "Breathe victory fire", cost: 800, owned: false },
            ].map((e, i) => (
              <div key={i} style={{ padding: "14px 12px", borderRadius: 16, background: e.owned ? C.greenSoft : C.card, border: `1.5px solid ${e.owned ? C.green + "33" : C.border}`, textAlign: "center" }}>
                <div style={{ fontSize: 30, marginBottom: 4 }}>{e.icon}</div>
                <div style={{ fontSize: 12, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{e.name}</div>
                <div style={{ fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", marginTop: 1 }}>{e.desc}</div>
                {e.owned ? <div style={{ marginTop: 6, fontSize: 10, color: C.green, fontWeight: 700 }}>✓ Active</div> : <div style={{ marginTop: 6, padding: "4px 10px", borderRadius: 8, background: C.accentSoft, fontSize: 10, fontWeight: 700, color: C.accent, display: "inline-block", cursor: "pointer" }}>⭐ {e.cost}</div>}
              </div>
            ))}
          </div>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 10, textTransform: "uppercase", letterSpacing: 0.8 }}>Correct Answer Sounds</div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 12 }}>
            {[
              { icon: "🔔", name: "Chime", cost: 0, owned: true },
              { icon: "🎺", name: "Fanfare", cost: 200, owned: false },
              { icon: "🎮", name: "Arcade", cost: 250, owned: false },
              { icon: "🎵", name: "Musical", cost: 300, owned: false },
            ].map((s, i) => (
              <div key={i} style={{ padding: "12px", borderRadius: 14, background: s.owned ? C.greenSoft : C.card, border: `1.5px solid ${s.owned ? C.green + "33" : C.border}`, display: "flex", alignItems: "center", gap: 10 }}>
                <span style={{ fontSize: 22 }}>{s.icon}</span>
                <div>
                  <div style={{ fontSize: 12, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{s.name}</div>
                  {s.owned ? <div style={{ fontSize: 9, color: C.green, fontWeight: 700 }}>✓ Active</div> : <div style={{ fontSize: 9, color: C.accent, fontWeight: 700 }}>⭐ {s.cost}</div>}
                </div>
              </div>
            ))}
          </div>
        </>}

        {tab === "titles" && <>
          <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 10, textTransform: "uppercase", letterSpacing: 0.8 }}>Earned Titles</div>
          <div style={{ fontSize: 11, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", marginBottom: 12 }}>Titles appear under your name. Earn them through achievements!</div>
          {[
            { title: "🌟 Rising Star", req: "Earn 100 total stars", unlocked: true },
            { title: "📝 Word Wizard", req: "Master 25 dictée words", unlocked: true },
            { title: "⚡ Speed Demon", req: "Average under 3s in Speed Math", unlocked: false },
            { title: "🔥 Streak Champion", req: "7-day login streak", unlocked: false },
            { title: "🏆 Perfect Scholar", req: "100% on 10 sessions", unlocked: false },
            { title: "💎 Star Collector", req: "Earn 5,000 total stars", unlocked: false },
            { title: "🌍 Polyglot", req: "Practice in all 3 languages", unlocked: false },
            { title: "🎓 Grand Master", req: "Unlock all other titles", unlocked: false },
          ].map((t, i) => (
            <div key={i} style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 14px", borderRadius: 14, background: t.unlocked ? C.accentSoft : C.card, border: `1.5px solid ${t.unlocked ? C.accent + "33" : C.border}`, marginBottom: 8, opacity: t.unlocked ? 1 : 0.55 }}>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 14, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{t.title}</div>
                <div style={{ fontSize: 10, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 1 }}>{t.req}</div>
              </div>
              {t.unlocked ? <div style={{ padding: "4px 10px", borderRadius: 8, background: C.accent, fontSize: 10, fontWeight: 700, color: "#FFF", cursor: "pointer" }}>Equip</div> : <div style={{ fontSize: 10, color: C.textMuted }}>🔒</div>}
            </div>
          ))}
        </>}
      </div>
      <NavBar active="rewards" onNav={onNav} />
    </div>
  );
};

/* ===================== SETTINGS ===================== */
const SettingsScreen = ({ onNav }) => (
  <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
    <StatusBar />
    <Header title="Settings" />
    <div style={{ flex: 1, overflowY: "auto", padding: "0 20px 12px" }}>
      {/* Profile card */}
      <div style={{ padding: "14px", borderRadius: 18, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 14, display: "flex", alignItems: "center", gap: 12, cursor: "pointer" }}>
        <AvatarDisplay body="unicorn" hat="party" face="shades" outfit="scarf" pet="chick" size={52} />
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 16, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif" }}>Léa</div>
          <div style={{ fontSize: 11, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>🌟 Rising Star</div>
        </div>
        <span style={{ color: C.textMuted, fontSize: 16 }}>›</span>
      </div>

      {/* General */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>General</div>
      <div style={{ borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, overflow: "hidden", marginBottom: 14 }}>
        {[
          { i: "🌍", l: "App Language", v: "Français" },
          { i: "🔔", l: "Study Reminders", v: "8:00 AM" },
          { i: "🔊", l: "Sound Effects", t: true, on: true },
          { i: "📳", l: "Haptic Feedback", t: true, on: true },
        ].map((r, i) => (
          <div key={i} style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderBottom: i < 3 ? `1px solid ${C.border}` : "none" }}>
            <span style={{ fontSize: 16 }}>{r.i}</span>
            <span style={{ flex: 1, fontSize: 13, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{r.l}</span>
            {r.t ? <Toggle on={r.on} /> : <span style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>{r.v} ›</span>}
          </div>
        ))}
      </div>

      {/* Voice packs */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Voice Packs (Offline TTS)</div>
      <div style={{ borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, overflow: "hidden", marginBottom: 14 }}>
        {[
          { f: "🇫🇷", l: "French", s: "28 MB", installed: true },
          { f: "🇬🇧", l: "English", s: "24 MB", installed: true },
          { f: "🇩🇪", l: "German", s: "26 MB", installed: false },
        ].map((v, i) => (
          <div key={i} style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderBottom: i < 2 ? `1px solid ${C.border}` : "none" }}>
            <span style={{ fontSize: 20 }}>{v.f}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{v.l}</div>
              <div style={{ fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>High quality · {v.s}</div>
            </div>
            {v.installed
              ? <div style={{ padding: "5px 10px", borderRadius: 8, background: C.greenSoft, fontSize: 10, fontWeight: 700, color: C.green }}>✓ Installed</div>
              : <div style={{ padding: "5px 10px", borderRadius: 8, background: C.secondarySoft, fontSize: 10, fontWeight: 700, color: C.secondary, cursor: "pointer" }}>Download</div>
            }
          </div>
        ))}
      </div>

      {/* Learning */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Learning</div>
      <div style={{ borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, overflow: "hidden", marginBottom: 14 }}>
        {[
          { i: "🎯", l: "Daily Goal", v: "5 activities" },
          { i: "✏️", l: "Accent Strictness", v: "Lenient" },
          { i: "⏱️", l: "Default Timer", v: "15s" },
          { i: "🔤", l: "Dictée Input", v: "Keyboard" },
          { i: "💡", l: "Show Hints", t: true, on: true },
        ].map((r, i) => (
          <div key={i} style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderBottom: i < 4 ? `1px solid ${C.border}` : "none" }}>
            <span style={{ fontSize: 16 }}>{r.i}</span>
            <span style={{ flex: 1, fontSize: 13, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{r.l}</span>
            {r.t ? <Toggle on={r.on} /> : <span style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>{r.v} ›</span>}
          </div>
        ))}
      </div>

      {/* Parent zone */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Parent Zone 🔒</div>
      <div style={{ borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, overflow: "hidden", marginBottom: 14 }}>
        {[
          { i: "📊", l: "Progress Reports" },
          { i: "⏰", l: "Screen Time Limits" },
          { i: "💾", l: "Backup & Export", highlight: true },
          { i: "☁️", l: "Cloud Sync", v: "Off" },
          { i: "🗑️", l: "Reset All Data", danger: true },
        ].map((r, i) => (
          <div key={i} style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderBottom: i < 4 ? `1px solid ${C.border}` : "none", cursor: "pointer" }}>
            <span style={{ fontSize: 16 }}>{r.i}</span>
            <span style={{ flex: 1, fontSize: 13, fontWeight: 600, color: r.danger ? C.danger : C.text, fontFamily: "'DM Sans',sans-serif" }}>{r.l}</span>
            <span style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>{r.v || ""} ›</span>
          </div>
        ))}
      </div>

      <div style={{ textAlign: "center", fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", padding: "4px 0" }}>StudyBuddy v1.0.0 · Made with ❤️</div>
    </div>
    <NavBar active="settings" onNav={onNav} />
  </div>
);

/* ===================== BACKUP & EXPORT ===================== */
const BackupExportScreen = ({ onBack }) => (
  <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
    <StatusBar />
    <Header title="Backup & Export" subtitle="Keep your data safe" onBack={onBack} />
    <div style={{ flex: 1, overflowY: "auto", padding: "0 20px 16px" }}>
      {/* Last backup */}
      <div style={{ padding: "16px", borderRadius: 18, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 16 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 12 }}>
          <div style={{ width: 42, height: 42, borderRadius: 12, background: C.greenSoft, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 22 }}>💾</div>
          <div>
            <div style={{ fontSize: 14, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>Last Backup</div>
            <div style={{ fontSize: 11, color: C.green, fontFamily: "'DM Sans',sans-serif", fontWeight: 600 }}>Feb 19, 2026 at 8:12 PM</div>
          </div>
        </div>
        <div style={{ display: "flex", gap: 8 }}>
          <div style={{ flex: 1, padding: "12px", borderRadius: 12, background: C.secondarySoft, textAlign: "center", fontSize: 13, fontWeight: 700, color: C.secondary, fontFamily: "'DM Sans',sans-serif", cursor: "pointer" }}>Backup Now</div>
          <div style={{ flex: 1, padding: "12px", borderRadius: 12, background: C.bgSoft, textAlign: "center", fontSize: 13, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", cursor: "pointer", border: `1.5px solid ${C.border}` }}>Restore</div>
        </div>
      </div>

      {/* Auto backup */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Automatic Backup</div>
      <div style={{ borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, overflow: "hidden", marginBottom: 16 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderBottom: `1px solid ${C.border}` }}>
          <span style={{ fontSize: 16 }}>🔄</span>
          <span style={{ flex: 1, fontSize: 13, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>Auto Backup</span>
          <Toggle on={true} />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px", borderBottom: `1px solid ${C.border}` }}>
          <span style={{ fontSize: 16 }}>⏰</span>
          <span style={{ flex: 1, fontSize: 13, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>Frequency</span>
          <span style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>Daily ›</span>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 10, padding: "12px 14px" }}>
          <span style={{ fontSize: 16 }}>📂</span>
          <span style={{ flex: 1, fontSize: 13, fontWeight: 600, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>Save Location</span>
          <span style={{ fontSize: 12, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>Device ›</span>
        </div>
      </div>

      {/* Export options */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Export Options</div>
      {[
        { icon: "📄", title: "Progress Report (PDF)", desc: "Printable summary with charts, scores, and teacher-friendly layout", color: C.primary },
        { icon: "📊", title: "Raw Data (JSON)", desc: "Full database export for migration or analysis", color: C.secondary },
        { icon: "📋", title: "Word Lists (CSV)", desc: "Export all dictée words as spreadsheet-compatible file", color: C.green },
        { icon: "📧", title: "Email Report", desc: "Send a weekly progress summary to parent's email", color: C.purple },
      ].map((e, i) => (
        <div key={i} style={{ display: "flex", alignItems: "center", gap: 12, padding: "14px 16px", borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 8, cursor: "pointer" }}>
          <div style={{ width: 40, height: 40, borderRadius: 12, background: `${e.color}15`, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20 }}>{e.icon}</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: e.color, fontFamily: "'DM Sans',sans-serif" }}>{e.title}</div>
            <div style={{ fontSize: 10, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 1, lineHeight: 1.4 }}>{e.desc}</div>
          </div>
          <span style={{ color: C.textMuted, fontSize: 14 }}>›</span>
        </div>
      ))}

      {/* Storage info */}
      <div style={{ padding: "12px 14px", borderRadius: 14, background: C.bgSoft, marginTop: 8 }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6 }}>
          <span style={{ fontSize: 11, fontWeight: 600, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>Local Storage Used</span>
          <span style={{ fontSize: 11, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>4.2 MB</span>
        </div>
        <div style={{ height: 5, borderRadius: 3, background: C.border, overflow: "hidden" }}>
          <div style={{ width: "8%", height: "100%", borderRadius: 3, background: C.secondary }} />
        </div>
        <div style={{ fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", marginTop: 4 }}>47 sessions · 3 word lists · 24 words · 2 voice packs</div>
      </div>
    </div>
  </div>
);

/* ===================== STATS ===================== */
const StatsScreen = ({ onNav }) => (
  <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
    <StatusBar />
    <Header title="My Progress" subtitle="Keep it up! 💪" />
    <div style={{ flex: 1, overflowY: "auto", padding: "0 20px 12px" }}>
      <div style={{ display: "flex", gap: 8, marginBottom: 14 }}>
        {[{ l: "Total Stars", v: "1,240", i: "⭐", bg: C.accentSoft }, { l: "Day Streak", v: "3", i: "🔥", bg: C.primarySoft }, { l: "Sessions", v: "47", i: "📚", bg: C.secondarySoft }].map((s, i) => (
          <div key={i} style={{ flex: 1, padding: "12px 6px", borderRadius: 14, background: s.bg, textAlign: "center" }}>
            <div style={{ fontSize: 14 }}>{s.i}</div>
            <div style={{ fontSize: 16, fontWeight: 800, color: C.text, fontFamily: "'Nunito',sans-serif" }}>{s.v}</div>
            <div style={{ fontSize: 9, color: C.textSoft, fontFamily: "'DM Sans',sans-serif" }}>{s.l}</div>
          </div>
        ))}
      </div>
      <div style={{ padding: "14px", borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 14 }}>
        <div style={{ fontSize: 12, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif", marginBottom: 12 }}>This Week</div>
        <div style={{ display: "flex", alignItems: "flex-end", justifyContent: "space-between", height: 90, gap: 5 }}>
          {[{d:"M",h:40},{d:"T",h:80},{d:"W",h:55},{d:"T",h:90},{d:"F",h:100},{d:"S",h:0},{d:"S",h:30}].map((d,i)=>(
            <div key={i} style={{ display: "flex", flexDirection: "column", alignItems: "center", flex: 1, gap: 3 }}>
              <div style={{ width: "100%", height: d.h, borderRadius: 6, background: i === 4 ? C.primary : C.primarySoft }} />
              <span style={{ fontSize: 9, fontWeight: 600, color: i === 4 ? C.primary : C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>{d.d}</span>
            </div>
          ))}
        </div>
      </div>
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Progress</div>
      {[{ m: "Dictée", s: "78% → 89% accuracy", t: "📈", c: C.primary }, { m: "Speed Math", s: "8.2s → 5.1s avg", t: "⚡", c: C.secondary }].map((m, i) => (
        <div key={i} style={{ padding: "12px 14px", borderRadius: 14, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 8, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div><div style={{ fontSize: 13, fontWeight: 700, color: m.c, fontFamily: "'DM Sans',sans-serif" }}>{m.m}</div><div style={{ fontSize: 11, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginTop: 1 }}>{m.s}</div></div>
          <span style={{ fontSize: 18 }}>{m.t}</span>
        </div>
      ))}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, marginTop: 12, textTransform: "uppercase", letterSpacing: 0.8 }}>Badges</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 6 }}>
        {[{ i: "⭐", l: "First 100", u: true }, { i: "🔥", l: "7-day", u: true }, { i: "📝", l: "50 Words", u: true }, { i: "⚡", l: "Speed", u: true }, { i: "🏆", l: "Perfect", u: false }, { i: "💎", l: "1000⭐", u: false }].map((b, i) => (
          <div key={i} style={{ padding: "6px 10px", borderRadius: 10, background: b.u ? C.accentSoft : C.bgSoft, border: `1.5px solid ${b.u ? C.accent + "33" : C.border}`, display: "flex", alignItems: "center", gap: 4, opacity: b.u ? 1 : 0.4 }}>
            <span style={{ fontSize: 14 }}>{b.i}</span>
            <span style={{ fontSize: 10, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{b.l}</span>
          </div>
        ))}
      </div>
    </div>
    <NavBar active="stats" onNav={onNav} />
  </div>
);

/* ===================== VOICE PACK MANAGER ===================== */
const VoicePackManager = ({ onBack }) => (
  <div style={{ height: "100%", display: "flex", flexDirection: "column", background: C.bg }}>
    <StatusBar />
    <Header title="Voice Packs" subtitle="Manage offline voices" onBack={onBack} />
    <div style={{ flex: 1, overflowY: "auto", padding: "0 20px 16px" }}>
      <div style={{ padding: "14px", borderRadius: 16, background: C.secondarySoft, border: `1.5px solid ${C.secondary}22`, marginBottom: 16, fontSize: 12, color: C.text, fontFamily: "'DM Sans',sans-serif", lineHeight: 1.5 }}>
        🔊 Voice packs let StudyBuddy read words aloud even without internet. Each pack includes a high-quality neural voice.
      </div>

      {/* Installed */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, textTransform: "uppercase", letterSpacing: 0.8 }}>Installed</div>
      {[
        { f: "🇫🇷", l: "French", voice: "Amélie (Neural)", s: "28 MB", q: "High" },
        { f: "🇬🇧", l: "English", voice: "Emma (Neural)", s: "24 MB", q: "High" },
      ].map((v, i) => (
        <div key={i} style={{ padding: "14px 16px", borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 8 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
            <span style={{ fontSize: 24 }}>{v.f}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 14, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{v.l}</div>
              <div style={{ fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>{v.voice} · {v.s}</div>
            </div>
            <div style={{ padding: "5px 10px", borderRadius: 8, background: C.greenSoft, fontSize: 10, fontWeight: 700, color: C.green }}>✓ Ready</div>
          </div>
          <div style={{ display: "flex", gap: 6 }}>
            <div style={{ padding: "8px 14px", borderRadius: 10, background: C.bgSoft, fontSize: 11, fontWeight: 600, color: C.secondary, fontFamily: "'DM Sans',sans-serif", cursor: "pointer" }}>🔊 Test</div>
            <div style={{ padding: "8px 14px", borderRadius: 10, background: C.bgSoft, fontSize: 11, fontWeight: 600, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", cursor: "pointer" }}>Update</div>
            <div style={{ padding: "8px 14px", borderRadius: 10, background: C.dangerSoft, fontSize: 11, fontWeight: 600, color: C.danger, fontFamily: "'DM Sans',sans-serif", cursor: "pointer" }}>Remove</div>
          </div>
        </div>
      ))}

      {/* Available */}
      <div style={{ fontSize: 11, fontWeight: 700, color: C.textSoft, fontFamily: "'DM Sans',sans-serif", marginBottom: 8, marginTop: 12, textTransform: "uppercase", letterSpacing: 0.8 }}>Available to Download</div>
      {[
        { f: "🇩🇪", l: "German", voice: "Marlene (Neural)", s: "26 MB" },
        { f: "🇪🇸", l: "Spanish", voice: "Lucía (Neural)", s: "25 MB" },
        { f: "🇮🇹", l: "Italian", voice: "Bianca (Neural)", s: "24 MB" },
      ].map((v, i) => (
        <div key={i} style={{ display: "flex", alignItems: "center", gap: 10, padding: "14px 16px", borderRadius: 16, background: C.card, border: `1.5px solid ${C.border}`, marginBottom: 8 }}>
          <span style={{ fontSize: 24 }}>{v.f}</span>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: C.text, fontFamily: "'DM Sans',sans-serif" }}>{v.l}</div>
            <div style={{ fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif" }}>{v.voice} · {v.s}</div>
          </div>
          <div style={{ padding: "6px 14px", borderRadius: 10, background: C.secondary, fontSize: 11, fontWeight: 700, color: "#FFF", fontFamily: "'DM Sans',sans-serif", cursor: "pointer" }}>Download</div>
        </div>
      ))}

      <div style={{ padding: "12px 14px", borderRadius: 14, background: C.bgSoft, marginTop: 8, fontSize: 10, color: C.textMuted, fontFamily: "'DM Sans',sans-serif", lineHeight: 1.5 }}>
        💡 Total storage used by voice packs: <strong>52 MB</strong><br/>
        Voices use Android's neural TTS engine for natural-sounding speech.
      </div>
    </div>
  </div>
);

/* ===================== MAIN APP ===================== */
const screens = [
  { id: "onboarding", label: "Onboarding + Avatar", component: OnboardingScreen },
  { id: "home", label: "Home", component: HomeScreen },
  { id: "avatar-closet", label: "Avatar Closet", component: AvatarCloset },
  { id: "rewards", label: "Rewards Shop", component: RewardsScreen },
  { id: "stats", label: "Stats", component: StatsScreen },
  { id: "settings", label: "Settings", component: SettingsScreen },
  { id: "backup", label: "Backup & Export", component: BackupExportScreen },
  { id: "voice-packs", label: "Voice Packs", component: VoicePackManager },
];

export default function StudyBuddyPrototype() {
  const [currentScreen, setCurrentScreen] = useState(0);
  const handleNav = useCallback((id) => {
    const idx = screens.findIndex(s => s.id === id);
    if (idx !== -1) setCurrentScreen(idx);
  }, []);

  const Screen = screens[currentScreen].component;
  return (
    <div style={{ minHeight: "100vh", background: "#111122", fontFamily: "'DM Sans',sans-serif" }}>
      <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@700;800;900&family=DM+Sans:wght@400;500;600;700&family=Caveat:wght@400;700&display=swap" rel="stylesheet" />
      <div style={{ padding: "28px 0 20px", textAlign: "center" }}>
        <div style={{ fontSize: 26, fontWeight: 900, color: "#FFF", fontFamily: "'Nunito',sans-serif" }}>📚 StudyBuddy <span style={{ fontSize: 13, fontWeight: 500, color: "rgba(255,255,255,0.3)", fontFamily: "'DM Sans',sans-serif" }}>v2 Prototype</span></div>
        <div style={{ fontSize: 12, color: "rgba(255,255,255,0.3)", fontFamily: "'DM Sans',sans-serif", marginTop: 4 }}>Tap screens below · Onboarding has 3 steps (tap Next)</div>
      </div>
      <div style={{ display: "flex", gap: 6, overflowX: "auto", padding: "0 20px 18px", scrollbarWidth: "none" }}>
        {screens.map((s, i) => (
          <button key={s.id} onClick={() => setCurrentScreen(i)} style={{ padding: "7px 14px", borderRadius: 10, border: "none", cursor: "pointer", fontSize: 11, fontWeight: 700, fontFamily: "'DM Sans',sans-serif", whiteSpace: "nowrap", background: i === currentScreen ? C.primary : "rgba(255,255,255,0.07)", color: i === currentScreen ? "#FFF" : "rgba(255,255,255,0.35)", transition: "all 0.2s" }}>
            {s.label}
          </button>
        ))}
      </div>
      <div style={{ display: "flex", justifyContent: "center", paddingBottom: 50 }}>
        <Phone label={screens[currentScreen].label}>
          <Screen onNav={handleNav} onBack={() => setCurrentScreen(1)} />
        </Phone>
      </div>
    </div>
  );
}
