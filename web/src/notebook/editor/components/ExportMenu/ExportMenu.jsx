import { useCallback, useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Download, File, FileText, FileType, LoaderCircle, X } from 'lucide-react';
import { useNotification } from '../../../../common/hooks/hooks';
import { exportToDocx, exportToPdf, exportToText } from '../../utils/exportUtils';
import { isAndroidHost } from '../../../../app/host/brainBoxHost';

const PAPER_SIZES = [
  { label: 'Letter  (8.5" x 11")', value: 'letter', width: 816, height: 1056 },
  { label: 'A4  (210mm x 297mm)', value: 'a4', width: 794, height: 1123 },
  { label: 'Legal  (8.5" x 14")', value: 'legal', width: 816, height: 1344 },
  { label: 'Tabloid  (11" x 17")', value: 'tabloid', width: 1056, height: 1632 },
];

const MARGIN_OPTIONS = [
  { label: 'Narrow  (0.5")', value: 0.5 },
  { label: 'Normal  (0.75")', value: 0.75 },
  { label: 'Moderate  (1")', value: 1 },
  { label: 'Wide  (1.5")', value: 1.5 },
];

const isEmbeddedAndroidHost = isAndroidHost();
const EXPORT_MODAL_MIN_VISIBLE_MS = 450;

const renderBodyPortal = (children) => (
  typeof document !== 'undefined'
    ? createPortal(children, document.body)
    : children
);

const waitForExportModalPaint = async () => {
  if (typeof window === 'undefined') {
    return;
  }

  await new Promise((resolve) => window.requestAnimationFrame(() => resolve()));
};

const waitForMinimumModalDuration = async (startedAt) => {
  const elapsed = Date.now() - startedAt;
  const remaining = EXPORT_MODAL_MIN_VISIBLE_MS - elapsed;

  if (remaining > 0) {
    await new Promise((resolve) => window.setTimeout(resolve, remaining));
  }
};

const PdfOptionsModal = ({ onClose, onExport }) => {
  const [paperSizeValue, setPaperSizeValue] = useState('letter');
  const [marginIn, setMarginIn] = useState(0.75);

  const selectedPaper = PAPER_SIZES.find((paper) => paper.value === paperSizeValue) || PAPER_SIZES[0];

  const handleExport = () => {
    onExport({
      paperWidth: selectedPaper.width,
      paperHeight: selectedPaper.height,
      marginIn,
    });
    onClose();
  };

  return (
    <div
      className="pdf-options-overlay"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div className="pdf-options-modal" role="dialog" aria-modal="true" aria-labelledby="pdf-options-title">
        <div className="pdf-options-header">
          <span id="pdf-options-title">PDF export options</span>
          <button type="button" className="pdf-options-close" onClick={onClose}>
            <X size={16} />
          </button>
        </div>
        <div className="pdf-options-body">
          <div className="pdf-options-field">
            <label htmlFor="pdf-paper-size">Paper size</label>
            <select
              id="pdf-paper-size"
              value={paperSizeValue}
              onChange={(event) => setPaperSizeValue(event.target.value)}
            >
              {PAPER_SIZES.map((paper) => (
                <option key={paper.value} value={paper.value}>{paper.label}</option>
              ))}
            </select>
          </div>
          <div className="pdf-options-field">
            <label htmlFor="pdf-margins">Margins</label>
            <select
              id="pdf-margins"
              value={marginIn}
              onChange={(event) => setMarginIn(Number(event.target.value))}
            >
              {MARGIN_OPTIONS.map((margin) => (
                <option key={margin.value} value={margin.value}>{margin.label}</option>
              ))}
            </select>
          </div>
          <p className="pdf-options-hint">
            Your browser print dialog will open. Choose "Save as PDF" as the destination to export a file.
          </p>
        </div>
        <div className="pdf-options-footer">
          <button type="button" className="pdf-options-btn pdf-options-btn--cancel" onClick={onClose}>
            Cancel
          </button>
          <button type="button" className="pdf-options-btn pdf-options-btn--confirm" onClick={handleExport}>
            <FileText size={14} />
            Print / Save as PDF
          </button>
        </div>
      </div>
    </div>
  );
};

const ExportProgressModal = ({ format }) => {
  const isWordExport = format === 'docx';
  const title = isWordExport ? 'Exporting Word document' : 'Preparing PDF export';
  const description = isWordExport
    ? 'Creating your .docx file. This may take a moment for larger notebooks.'
    : isEmbeddedAndroidHost
      ? 'Preparing the notebook for PDF sharing.'
      : 'Preparing the print dialog so you can save the notebook as a PDF.';
  const Icon = isWordExport ? FileType : FileText;

  return (
    <div className="export-progress-overlay" role="presentation">
      <div className="export-progress-modal" role="dialog" aria-modal="true" aria-labelledby="export-progress-title">
        <div className="export-progress-icon" aria-hidden="true">
          <Icon size={22} />
        </div>
        <div className="export-progress-copy">
          <h2 id="export-progress-title">{title}</h2>
          <p>{description}</p>
        </div>
        <LoaderCircle className="export-progress-spinner" size={24} aria-hidden="true" />
      </div>
    </div>
  );
};

const ExportMenu = ({
  getContent,
  getLayout,
  title = 'Untitled',
  buttonClassName = 'editor-navbar-icon-btn',
  wrapClassName = '',
  dropdownPlacement = 'bottom',
}) => {
  const [open, setOpen] = useState(false);
  const [exporting, setExporting] = useState(null);
  const [showPdfOptions, setShowPdfOptions] = useState(false);
  const [portalStyle, setPortalStyle] = useState(null);
  const ref = useRef(null);
  const buttonRef = useRef(null);
  const dropdownRef = useRef(null);
  const { addNotification } = useNotification();
  const usePortalDropdown = dropdownPlacement === 'portal-top';

  const updatePortalPosition = useCallback(() => {
    if (!usePortalDropdown || typeof window === 'undefined') {
      return;
    }

    const rect = buttonRef.current?.getBoundingClientRect();
    if (!rect) {
      return;
    }

    const menuWidth = 220;
    const gutter = 10;
    const left = Math.min(
      window.innerWidth - menuWidth - gutter,
      Math.max(gutter, rect.right - menuWidth),
    );

    setPortalStyle({
      position: 'fixed',
      left: `${left}px`,
      bottom: `${window.innerHeight - rect.top + 8}px`,
      minWidth: `${menuWidth}px`,
    });
  }, [usePortalDropdown]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      const clickedInsideWrap = ref.current && ref.current.contains(event.target);
      const clickedInsideDropdown = dropdownRef.current && dropdownRef.current.contains(event.target);

      if (!clickedInsideWrap && !clickedInsideDropdown) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  useEffect(() => {
    if (!open || !usePortalDropdown) {
      setPortalStyle(null);
      return undefined;
    }

    updatePortalPosition();
    window.addEventListener('resize', updatePortalPosition);
    window.addEventListener('scroll', updatePortalPosition, true);

    return () => {
      window.removeEventListener('resize', updatePortalPosition);
      window.removeEventListener('scroll', updatePortalPosition, true);
    };
  }, [open, updatePortalPosition, usePortalDropdown]);

  const handleExport = async (format, pdfOptions = {}) => {
    setExporting(format);
    const modalStartedAt = Date.now();

    try {
      if (format === 'print' || format === 'docx') {
        await waitForExportModalPaint();
      }

      const html = getContent();
      const layout = getLayout?.();

      if (format === 'print') {
        await exportToPdf(html, title, { ...layout, ...pdfOptions });
      } else if (format === 'docx') {
        await exportToDocx(html, title);
      } else if (format === 'txt') {
        await exportToText(html, title);
      }
    } catch (error) {
      addNotification(error.message || 'Export failed. Please try again.', 'error', 4000);
    } finally {
      if (format === 'print' || format === 'docx') {
        await waitForMinimumModalDuration(modalStartedAt);
      }

      setExporting(null);
      setOpen(false);
    }
  };

  const dropdown = (
    <div
      ref={dropdownRef}
      className={`export-menu-dropdown ${usePortalDropdown ? 'export-menu-dropdown--portal' : ''}`.trim()}
      style={usePortalDropdown ? (portalStyle || { visibility: 'hidden' }) : undefined}
    >
      <button type="button" onClick={() => {
        if (isEmbeddedAndroidHost) {
          void handleExport('print');
          return;
        }

        setOpen(false);
        setShowPdfOptions(true);
      }} disabled={Boolean(exporting)}>
        <FileText size={14} />
        {isEmbeddedAndroidHost ? 'Print / Share PDF' : 'Print / Save as PDF'}
      </button>
      <button type="button" onClick={() => handleExport('docx')} disabled={Boolean(exporting)}>
        <FileType size={14} />
        {exporting === 'docx' ? 'Exporting...' : 'Export as Word (.docx)'}
      </button>
      <button type="button" onClick={() => handleExport('txt')} disabled={Boolean(exporting)}>
        <File size={14} />
        {exporting === 'txt' ? 'Exporting...' : 'Export as Text (.txt)'}
      </button>
    </div>
  );

  return (
    <>
      <div className={`export-menu-wrap ${wrapClassName}`.trim()} ref={ref}>
        <button
          ref={buttonRef}
          type="button"
          className={buttonClassName}
          onClick={() => setOpen((value) => !value)}
          title="Export"
          aria-label="Export"
        >
          <Download size={16} strokeWidth={1.75} />
        </button>

        {open && (
          usePortalDropdown && typeof document !== 'undefined'
            ? createPortal(dropdown, document.body)
            : dropdown
        )}
      </div>

      {!isEmbeddedAndroidHost && showPdfOptions && (
        renderBodyPortal(
          <PdfOptionsModal
            onClose={() => setShowPdfOptions(false)}
            onExport={(options) => handleExport('print', options)}
          />
        )
      )}

      {(exporting === 'print' || exporting === 'docx') && (
        renderBodyPortal(<ExportProgressModal format={exporting} />)
      )}
    </>
  );
};

export default ExportMenu;
