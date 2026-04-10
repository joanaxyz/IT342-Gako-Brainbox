import { useEffect, useRef, useState } from 'react';
import { Download, File, FileText, FileType, X } from 'lucide-react';
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
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div className="pdf-options-modal">
        <div className="pdf-options-header">
          <span>PDF export options</span>
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

const ExportMenu = ({ getContent, getLayout, title = 'Untitled' }) => {
  const [open, setOpen] = useState(false);
  const [exporting, setExporting] = useState(null);
  const [showPdfOptions, setShowPdfOptions] = useState(false);
  const ref = useRef(null);
  const { addNotification } = useNotification();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (ref.current && !ref.current.contains(event.target)) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const handleExport = async (format, pdfOptions = {}) => {
    setExporting(format);

    try {
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
      setExporting(null);
      setOpen(false);
    }
  };

  return (
    <>
      <div className="export-menu-wrap" ref={ref}>
        <button
          type="button"
          className="editor-navbar-icon-btn"
          onClick={() => setOpen((value) => !value)}
          title="Export"
          aria-label="Export"
        >
          <Download size={16} strokeWidth={1.75} />
        </button>

        {open && (
          <div className="export-menu-dropdown">
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
        )}
      </div>

      {!isEmbeddedAndroidHost && showPdfOptions && (
        <PdfOptionsModal
          onClose={() => setShowPdfOptions(false)}
          onExport={(options) => handleExport('print', options)}
        />
      )}
    </>
  );
};

export default ExportMenu;
  const isEmbeddedAndroidHost = isAndroidHost();
