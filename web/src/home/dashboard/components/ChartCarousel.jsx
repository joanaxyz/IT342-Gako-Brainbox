import { useState } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const ChartCarousel = ({ children }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const charts = Array.isArray(children) ? children : [children];

  const goToPrevious = () => {
    setCurrentIndex((prev) => (prev === 0 ? charts.length - 1 : prev - 1));
  };

  const goToNext = () => {
    setCurrentIndex((prev) => (prev === charts.length - 1 ? 0 : prev + 1));
  };

  if (charts.length === 0) return null;

  return (
    <div className="chart-carousel">
      <div className="carousel-container">
        <div className="carousel-track" style={{ transform: `translateX(-${currentIndex * 100}%)` }}>
          {charts.map((chart, index) => (
            <div key={index} className="carousel-slide">
              {chart}
            </div>
          ))}
        </div>
      </div>
      
      {charts.length > 1 && (
        <>
          <button 
            className="carousel-nav carousel-nav-prev" 
            onClick={goToPrevious}
            aria-label="Previous chart"
          >
            <ChevronLeft size={16} />
          </button>
          
          <button 
            className="carousel-nav carousel-nav-next" 
            onClick={goToNext}
            aria-label="Next chart"
          >
            <ChevronRight size={16} />
          </button>
          
          <div className="carousel-indicators">
            {charts.map((_, index) => (
              <button
                key={index}
                className={`indicator ${index === currentIndex ? 'active' : ''}`}
                onClick={() => setCurrentIndex(index)}
                aria-label={`Go to chart ${index + 1}`}
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default ChartCarousel;
