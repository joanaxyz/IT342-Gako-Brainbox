import { ChevronDown, ChevronUp, ChevronsLeft, ChevronsRight } from 'lucide-react';

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';

import { buildOutlineTree, getExpandableOutlineKeys } from './outlineTree';



const OUTLINE_NAV_COLLAPSED_STORAGE_KEY = 'noteEditorOutlineNavCollapsed';

const MOBILE_BREAKPOINT_QUERY = '(max-width: 1280px)';



const OutlineNav = ({

  outline = [],

  onSelect,

  title = 'Navigator',

  emptyMessage = 'No headings yet. Use # to create one.',

  isItemActive,

  onSelectItem,

  mobileOverlayOpen,

  onMobileOverlayOpenChange,

}) => {

  const [isCollapsed, setIsCollapsed] = useState(() => (

    localStorage.getItem(OUTLINE_NAV_COLLAPSED_STORAGE_KEY) === 'true'

  ));

  const [isMobile, setIsMobile] = useState(() => (

    typeof window !== 'undefined' && window.matchMedia(MOBILE_BREAKPOINT_QUERY).matches

  ));



  useEffect(() => {

    const mediaQuery = window.matchMedia(MOBILE_BREAKPOINT_QUERY);

    const handleChange = (e) => setIsMobile(e.matches);

    mediaQuery.addEventListener('change', handleChange);

    return () => mediaQuery.removeEventListener('change', handleChange);

  }, []);

  const [internalMobileOverlayOpen, setInternalMobileOverlayOpen] = useState(false);

  const [expandedItemKeys, setExpandedItemKeys] = useState(() => new Set());

  const knownExpandableKeysRef = useRef(new Set());

  const isMobileOverlayControlled = typeof mobileOverlayOpen === 'boolean' && typeof onMobileOverlayOpenChange === 'function';

  const resolvedMobileOverlayOpen = isMobileOverlayControlled ? mobileOverlayOpen : internalMobileOverlayOpen;

  const outlineTree = useMemo(() => buildOutlineTree(outline), [outline]);

  const expandableKeys = useMemo(() => getExpandableOutlineKeys(outlineTree), [outlineTree]);

  const expandableKeySignature = expandableKeys.join('\n');



  const setMobileOverlayOpen = useCallback((nextValue) => {

    if (isMobileOverlayControlled) {

      onMobileOverlayOpenChange(nextValue);

      return;

    }



    setInternalMobileOverlayOpen(nextValue);

  }, [isMobileOverlayControlled, onMobileOverlayOpenChange]);



  const isExpanded = isMobile ? resolvedMobileOverlayOpen : !isCollapsed;



  const closeMobileOverlay = useCallback(() => {

    setMobileOverlayOpen(false);

  }, [setMobileOverlayOpen]);



  const handleToggle = useCallback(() => {

    if (isMobile) {

      setMobileOverlayOpen(!resolvedMobileOverlayOpen);

      return;

    }



    setIsCollapsed((value) => !value);

  }, [isMobile, resolvedMobileOverlayOpen, setMobileOverlayOpen]);



  const handleSelect = useCallback((item) => {

    if (onSelectItem) {

      onSelectItem(item);

    } else {

      onSelect?.(item.pos);

    }



    if (isMobile) {

      closeMobileOverlay();

    }

  }, [closeMobileOverlay, isMobile, onSelect, onSelectItem]);



  const handleToggleItem = useCallback((key) => {

    setExpandedItemKeys((currentKeys) => {

      const nextKeys = new Set(currentKeys);



      if (nextKeys.has(key)) {

        nextKeys.delete(key);

      } else {

        nextKeys.add(key);

      }



      return nextKeys;

    });

  }, []);



  useEffect(() => {

    localStorage.setItem(OUTLINE_NAV_COLLAPSED_STORAGE_KEY, String(isCollapsed));

  }, [isCollapsed]);



  useEffect(() => {

    const previousExpandableKeys = knownExpandableKeysRef.current;

    const nextExpandableKeys = new Set(expandableKeys);



    setExpandedItemKeys((currentKeys) => {

      const nextKeys = new Set(

        Array.from(currentKeys).filter((key) => nextExpandableKeys.has(key))

      );



      expandableKeys.forEach((key) => {

        if (!previousExpandableKeys.has(key)) {

          nextKeys.add(key);

        }

      });



      return nextKeys;

    });



    knownExpandableKeysRef.current = nextExpandableKeys;

  }, [expandableKeySignature, expandableKeys]);



  useEffect(() => {

    if (typeof window === 'undefined') {

      return undefined;

    }



    const mediaQuery = window.matchMedia(MOBILE_BREAKPOINT_QUERY);

    const handleMediaQueryChange = (event) => {

      setIsMobile(event.matches);

    };



    mediaQuery.addEventListener('change', handleMediaQueryChange);



    return () => {

      mediaQuery.removeEventListener('change', handleMediaQueryChange);

    };

  }, []);



  useEffect(() => {

    if (!isMobile) {

      window.queueMicrotask(() => {

        setMobileOverlayOpen(false);

      });

    }

  }, [isMobile, setMobileOverlayOpen]);



  useEffect(() => {

    if (!isMobile || !isExpanded) {

      return undefined;

    }



    const handleKeyDown = (event) => {

      if (event.key === 'Escape') {

        closeMobileOverlay();

      }

    };



    document.addEventListener('keydown', handleKeyDown);



    return () => {

      document.removeEventListener('keydown', handleKeyDown);

    };

  }, [closeMobileOverlay, isExpanded, isMobile]);



  function renderOutlineNode(item, depth = 0) {

    const hasChildren = item.children.length > 0;

    const isItemExpanded = hasChildren && expandedItemKeys.has(item.key);

    const itemActive = isItemActive?.(item, item.originalIndex) ?? false;



    return (

      <div

        key={item.key}

        className={[

          'outline-item',

          `level-${item.level}`,

          hasChildren ? 'has-children' : 'has-no-children',

          isItemExpanded ? 'is-item-expanded' : 'is-item-collapsed',

          itemActive ? 'is-active' : '',

        ].filter(Boolean).join(' ')}

        style={{ '--outline-depth': depth }}

      >

        <div className="outline-item-row">

          {hasChildren ? (

            <button

              type="button"

              className="outline-item-toggle"

              onClick={() => handleToggleItem(item.key)}

              aria-label={isItemExpanded ? `Retract ${item.text}` : `Expand ${item.text}`}

              title={isItemExpanded ? 'Retract section' : 'Expand section'}

              aria-expanded={isItemExpanded}

            >

              {isItemExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}

            </button>

          ) : (

            <span className="outline-item-toggle-spacer" aria-hidden="true" />

          )}



          <button

            type="button"

            className="outline-item-button"

            onClick={() => handleSelect(item)}

            title={item.text}

            aria-current={itemActive ? 'true' : undefined}

          >

            <span className="outline-item-text">{item.text}</span>

          </button>

        </div>



        {hasChildren && isItemExpanded && (

          <div className="outline-children">

            {item.children.map((child) => renderOutlineNode(child, depth + 1))}

          </div>

        )}

      </div>

    );

  }



  return (

    <>

      <aside

        className={[

          'outline-sidebar',

          isExpanded ? 'is-expanded' : 'is-collapsed',

          isMobile ? 'is-mobile-overlay' : '',

          isMobile && isExpanded ? 'is-mobile-overlay-open' : '',

        ].filter(Boolean).join(' ')}

        aria-hidden={isMobile && !isExpanded ? 'true' : undefined}

      >

        <div className="outline-sidebar-header">

          <button

            type="button"

            className="outline-sidebar-toggle"

            onClick={handleToggle}

            aria-label={isExpanded ? 'Collapse navigator' : 'Expand navigator'}

            title={isExpanded ? 'Collapse navigator' : 'Expand navigator'}

          >

            {isExpanded ? <ChevronsLeft size={15} /> : <ChevronsRight size={15} />}

          </button>

          <span className="outline-sidebar-title">{title}</span>

          <span className="outline-sidebar-count">{outline.length}</span>

        </div>



        <nav className="outline-nav">

          {outline.length === 0 ? (

            <div className="outline-empty">{isExpanded ? emptyMessage : '0'}</div>

          ) : (

            <div className="outline-list">

              {outlineTree.map((item) => renderOutlineNode(item))}

            </div>

          )}

        </nav>

      </aside>

    </>

  );

};



export default OutlineNav;

