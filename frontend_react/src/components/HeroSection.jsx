// HeroSection.jsx
function HeroSection() {
    return (
      <section className="hero-section">
        <img
          src="https://source.unsplash.com/featured/?livingroom"
          alt="Living Room"
          className="hero-image"
        />
        <div className="hero-overlay">
          <h1 className="hero-title">The Art of Living Danishly</h1>
          <p className="hero-text">Elevating this minimalist living room with Carmo sofa.</p>
          <button className="hero-button">DISCOVER MORE</button>
        </div>
      </section>
    );
  }
  export default HeroSection;