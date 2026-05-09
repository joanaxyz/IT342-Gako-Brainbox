// Responsive pagination utilities
export const getResponsivePageSize = (basePageSize, screenSize = 'desktop') => {
  const pageSizeMap = {
    desktop: basePageSize,
    tablet: Math.max(Math.floor(basePageSize * 0.75), 4),
    mobile: Math.max(Math.floor(basePageSize * 0.5), 3)
  };
  
  return pageSizeMap[screenSize] || basePageSize;
};

export const getScreenSize = () => {
  if (typeof window === 'undefined') return 'desktop';
  
  const width = window.innerWidth;
  if (width <= 640) return 'mobile';
  if (width <= 900) return 'tablet';
  return 'desktop';
};

export const useResponsivePageSize = (basePageSize) => {
  const [pageSize, setPageSize] = useState(() => 
    getResponsivePageSize(basePageSize, getScreenSize())
  );

  useEffect(() => {
    const handleResize = () => {
      const newPageSize = getResponsivePageSize(basePageSize, getScreenSize());
      setPageSize(newPageSize);
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [basePageSize]);

  return pageSize;
};
