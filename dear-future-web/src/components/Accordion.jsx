import { createContext, useContext, useState } from 'react';
import { FaChevronDown } from 'react-icons/fa';
import './Accordion.css';

const AccordionContext = createContext({ setOpenValue: () => {}, isOpen: () => false });
const AccordionItemContext = createContext(null);

export const Accordion = ({ children, multiple = false, className = '' }) => {
  const [openValue, setOpenValue] = useState(null);

  const toggle = (value) => {
    setOpenValue((prev) => {
      if (multiple) {
        const next = new Set(Array.isArray(prev) ? prev : prev != null ? [prev] : []);
        if (next.has(value)) next.delete(value);
        else next.add(value);
        return next.size ? Array.from(next) : null;
      }
      return prev === value ? null : value;
    });
  };

  const isOpen = (value) => {
    if (multiple && Array.isArray(openValue)) return openValue.includes(value);
    return openValue === value;
  };

  return (
    <AccordionContext.Provider value={{ setOpenValue: toggle, isOpen }}>
      <div className={`accordion ${className}`.trim()} data-accordion>
        {children}
      </div>
    </AccordionContext.Provider>
  );
};

export const AccordionItem = ({ children, value, className = '' }) => {
  return (
    <AccordionItemContext.Provider value={value}>
      <div className={`accordion-item ${className}`.trim()} data-accordion-item data-value={value}>
        {children}
      </div>
    </AccordionItemContext.Provider>
  );
};

export const AccordionTrigger = ({ children, showArrow = true, className = '' }) => {
  const { isOpen, setOpenValue } = useContext(AccordionContext);
  const value = useContext(AccordionItemContext);

  const handleClick = () => {
    if (value != null) setOpenValue(value);
  };

  const open = value != null ? isOpen(value) : false;

  return (
    <button
      type="button"
      className={`accordion-trigger ${className}`.trim()}
      onClick={handleClick}
      aria-expanded={open}
      data-state={open ? 'open' : 'closed'}
    >
      <span className="accordion-trigger-text">{children}</span>
      {showArrow && <FaChevronDown className="accordion-trigger-icon" aria-hidden />}
    </button>
  );
};

export const AccordionPanel = ({ children, keepRendered = false, className = '' }) => {
  const { isOpen } = useContext(AccordionContext);
  const value = useContext(AccordionItemContext);
  const open = value != null ? isOpen(value) : false;

  return (
    <div
      className={`accordion-panel ${className}`.trim()}
      data-state={open ? 'open' : 'closed'}
      aria-hidden={!open}
    >
      <div className="accordion-panel-inner">
        {(keepRendered || open) ? children : null}
      </div>
    </div>
  );
};
