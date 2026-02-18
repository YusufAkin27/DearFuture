import { createContext, useContext, useCallback, useRef } from 'react';
import './PinInput.css';

const PinInputContext = createContext(null);

const usePinInput = () => {
    const ctx = useContext(PinInputContext);
    if (!ctx) throw new Error('PinInput components must be used within PinInput');
    return ctx;
};

export const PinInput = ({ children, value = '', onChange, size = 'md' }) => {
    const contextValue = {
        value: (value || '').toString(),
        onChange,
        size,
    };
    return (
        <PinInputContext.Provider value={contextValue}>
            <div className={`pin-input pin-input--${size}`} data-pin-input>
                {children}
            </div>
        </PinInputContext.Provider>
    );
};

PinInput.Label = function PinInputLabel({ children, ...props }) {
    return (
        <label className="pin-input__label" {...props}>
            {children}
        </label>
    );
};

PinInput.Group = function PinInputGroup({ children, maxLength = 6 }) {
    const { value, onChange, size } = usePinInput();
    const refs = useRef([]);
    const raw = (value || '').toString().slice(0, maxLength);
    const padded = raw.padEnd(maxLength, ' ');

    const setSlotValue = useCallback(
        (index, digit) => {
            const d = (digit || '').toString().replace(/\D/g, '').slice(0, 1);
            const arr = padded.split('').map((c, i) => (i === index ? (d || ' ') : /^\d$/.test(c) ? c : ' '));
            const newValue = arr.join('');
            onChange?.(newValue);
        },
        [value, maxLength, onChange]
    );

    const handleKeyDown = useCallback(
        (index, e) => {
            if (e.key === 'Backspace' && !value[index] && index > 0) {
                refs.current[index - 1]?.focus();
                setSlotValue(index - 1, '');
                return;
            }
            /* Enter: formu gönder (verify sayfası vb.) */
            if (e.key === 'Enter') {
                const digitsOnly = (value || '').replace(/\D/g, '').slice(0, maxLength);
                if (digitsOnly.length === maxLength) {
                    e.preventDefault();
                    const form = e.target?.closest?.('form');
                    if (form) form.requestSubmit();
                }
            }
        },
        [value, setSlotValue, maxLength]
    );

    const handlePaste = useCallback(
        (e) => {
            e.preventDefault();
            const pasted = (e.clipboardData?.getData('text') || '').replace(/\D/g, '').slice(0, maxLength);
            if (pasted) onChange?.(pasted);
            const nextIndex = Math.min(pasted.length, maxLength - 1);
            refs.current[nextIndex]?.focus();
        },
        [maxLength, onChange]
    );

    return (
        <PinInputContext.Provider
            value={{
                ...usePinInput(),
                maxLength,
                setSlotValue,
                handleKeyDown,
                handlePaste,
                refs,
            }}
        >
            <div className="pin-input__group" onPaste={handlePaste}>
                {children}
            </div>
        </PinInputContext.Provider>
    );
};

PinInput.Slot = function PinInputSlot({ index }) {
    const { value, setSlotValue, handleKeyDown, maxLength, refs, size } = usePinInput();
    const ch = value[index];
    const digit = (ch && ch !== ' ') ? ch : '';

    const handleChange = (e) => {
        const v = e.target.value.replace(/\D/g, '').slice(-1);
        setSlotValue(index, v);
        if (v && index < maxLength - 1) refs.current[index + 1]?.focus();
    };

    const handleKeyDownSlot = (e) => {
        handleKeyDown(index, e);
        if (e.key >= '0' && e.key <= '9' && digit && index < maxLength - 1) {
            setSlotValue(index, e.key);
            refs.current[index + 1]?.focus();
        }
    };

    return (
        <input
            ref={(el) => (refs.current[index] = el)}
            type="text"
            inputMode="numeric"
            maxLength={1}
            className={`pin-input__slot pin-input__slot--${size}`}
            value={digit}
            onChange={handleChange}
            onKeyDown={handleKeyDownSlot}
            aria-label={`Digit ${index + 1}`}
        />
    );
};

PinInput.Separator = function PinInputSeparator() {
    return <span className="pin-input__separator" aria-hidden="true" />;
};

PinInput.Description = function PinInputDescription({ children }) {
    return <p className="pin-input__description">{children}</p>;
};
