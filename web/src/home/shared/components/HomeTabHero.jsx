import '../styles/homeTabHero.css';

const HomeTabHero = ({
  label,
  title,
  description,
  meta,
  icon = null,
  actions = null,
  gradient,
  className = '',
}) => (
  <section
    className={`home-tab-hero ${className}`.trim()}
    style={gradient ? { '--tab-hero-gradient': gradient } : undefined}
  >
    <div className="home-tab-hero__main">
      <div className="home-tab-hero__copy">
        {label ? <span className="home-tab-hero__label">{label}</span> : null}
        <h1 className="home-tab-hero__title">{title}</h1>
        {description ? <p className="home-tab-hero__description">{description}</p> : null}
        {meta ? <span className="home-tab-hero__meta">{meta}</span> : null}
      </div>

      {(actions || icon) ? (
        <div className="home-tab-hero__side">
          {actions ? <div className="home-tab-hero__actions">{actions}</div> : null}
          {icon ? <div className="home-tab-hero__art">{icon}</div> : null}
        </div>
      ) : null}
    </div>
  </section>
);

export default HomeTabHero;
