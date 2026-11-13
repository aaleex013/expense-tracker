-- Los gastos recurrentes existentes seguirán siendo mensuales.
ALTER TABLE recurring_expenses
    ADD COLUMN frequency VARCHAR(10) NOT NULL DEFAULT 'MONTHLY';

-- Guardamos el mes del año solo para las recurrencias anuales.
ALTER TABLE recurring_expenses
    ADD COLUMN month_of_year SMALLINT;

-- Solo permitimos las frecuencias implementadas.
ALTER TABLE recurring_expenses
    ADD CONSTRAINT chk_recurring_expenses_frequency
        CHECK (frequency IN ('MONTHLY', 'YEARLY'));

-- Los mensuales no necesitan mes del año.
-- Los anuales deben indicar un mes entre enero y diciembre.
ALTER TABLE recurring_expenses
    ADD CONSTRAINT chk_recurring_expenses_month_of_year
        CHECK (
            (frequency = 'MONTHLY' AND month_of_year IS NULL)
                OR
            (
                frequency = 'YEARLY'
                    AND month_of_year IS NOT NULL
                    AND month_of_year BETWEEN 1 AND 12
                )
            );