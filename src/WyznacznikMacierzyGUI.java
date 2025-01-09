import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WyznacznikMacierzyGUI {

    //© Olaf Olejnik, 122681, Społeczna Akademia Nauk, Łódź, Poland

    // Główna metoda uruchamiająca aplikację
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WyznacznikMacierzyGUI().stworzOkno());
    }

    //Metody realizujące GUI - od 21 do 356 linii

    // Metoda tworząca główne okno aplikacji
    private void stworzOkno() {
        JFrame frame = new JFrame("Kalkulator Wyznacznika Macierzy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(750, 500));
        frame.setResizable(true);

        // Panel główny
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Pole tekstowe do wprowadzania rozmiaru macierzy
        JTextField rozmiarField = new JTextField(5);

        // Dodanie filtra do rozmiarField, aby akceptował tylko cyfry od 1 do 6 lub pustą wartość
        ((PlainDocument) rozmiarField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = new StringBuilder(currentText).replace(offset, offset + length, text).toString();
                if (isAllowedInput(newText)) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    pokazBlad(fb);
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = new StringBuilder(currentText).insert(offset, string).toString();
                if (isAllowedInput(newText)) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    pokazBlad(fb);
                }
            }

            private void pokazBlad(FilterBypass fb) {
                try {
                    fb.remove(0, fb.getDocument().getLength()); // Czyści całe pole
                    JOptionPane.showMessageDialog(null, "Dozwolone wartości to tylko liczby od 1 do 6!", "Błąd", JOptionPane.ERROR_MESSAGE);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }

            private boolean isAllowedInput(String newText) {
                String currentText = "";
                try {
                    currentText = rozmiarField.getText(0, rozmiarField.getDocument().getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                // Scal obecny tekst i nowy tekst
                String combinedText = currentText + newText;
                // Sprawdź, czy tekst składa się wyłącznie z unikalnych cyfr 1-6
                return combinedText.matches("^[1-6]{1,6}$") && hasUniqueCharacters(combinedText);
            }

            private boolean hasUniqueCharacters(String text) {
                return text.chars().distinct().count() == text.length();
            }


            private void pokazBlad() {
                JOptionPane.showMessageDialog(frame, "Dozwolone wartości to tylko liczby od 1 do 6!", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Panel do wprowadzania danych macierzy
        JPanel macierzPanel = new JPanel();

        // Przycisk do obliczania wyznacznika
        JButton obliczPrzycisk = new JButton("Oblicz wyznacznik");

        // Pole wyboru metody obliczania
        String[] metody = {"Laplace", "Gauss", "Sarrus"};
        JComboBox<String> metodaCombo = new JComboBox<>(metody);

        // Pole wyboru wiersza lub kolumny dla metody Laplace'a
        JComboBox<String> wyborLaplaceCombo = new JComboBox<>(new String[] {"Wiersz", "Kolumna"});
        JComboBox<Integer> indeksCombo = new JComboBox<>();

        // Etykieta wynikowa
        JLabel wynikLabel = new JLabel("Wyznacznik: ");

        // Górny panel
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Rozmiar macierzy: "));
        topPanel.add(rozmiarField);

        // Przycisk "Wstaw losowe liczby"
        JButton losowePrzycisk = new JButton("Wstaw losowe liczby");
        losowePrzycisk.addActionListener(e -> {
            Component[] pola = macierzPanel.getComponents();
            for (Component pole : pola) {
                if (pole instanceof JTextField) {
                    int losowaLiczba = (int) (Math.random() * 201) - 100; // Losowa liczba od -100 do 100
                    ((JTextField) pole).setText(String.valueOf(losowaLiczba));
                }
            }
        });
        topPanel.add(losowePrzycisk);

        // Obsługa zmian w polu tekstowym rozmiarField
        rozmiarField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                generujMacierz();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                generujMacierz();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                generujMacierz();
            }


            private void generujMacierz() {
                macierzPanel.removeAll();
                indeksCombo.removeAllItems();
                try {
                    String input = rozmiarField.getText();

                    // Jeśli pole jest puste, po prostu zakończ działanie metody
                    if (input.isEmpty()) {
                        frame.revalidate();
                        frame.repaint();
                        return;
                    }

                    // Sprawdza, czy wprowadzono tylko dozwolone wartości (1-6)
                    if (!input.matches("[1-6]")) {
                        throw new IllegalArgumentException("Dozwolone rozmiary macierzy to tylko 1, 2, 3, 4, 5, 6.");
                    }

                    int rozmiar = Integer.parseInt(input);

                    macierzPanel.setLayout(new GridLayout(rozmiar, rozmiar));

                    for (int i = 0; i < rozmiar * rozmiar; i++) {
                        JTextField pole = new JTextField(3);
                        pole.setText("0");
                        macierzPanel.add(pole);
                    }

                    for (int i = 1; i <= rozmiar; i++) {
                        indeksCombo.addItem(i);
                    }

                    // Ustawienie rozmiaru okna względem liczby elementów w macierzy
                    int newHeight = 100 + rozmiar * 30; // Dostosuj wartość
                    int newWidth = 200 + rozmiar * 30;
                    SwingUtilities.invokeLater(() -> {
                        frame.setSize(Math.max(newWidth, 500), Math.max(newHeight, 400));
                        frame.revalidate();
                        frame.repaint();
                    });

                } catch (NumberFormatException ex) {
                    macierzPanel.removeAll();
                    frame.revalidate();
                    frame.repaint();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                    rozmiarField.setText(""); // Czyści pole w przypadku błędu
                }
            }


        });


        // Obsługa przycisku obliczania wyznacznika
        obliczPrzycisk.addActionListener(e -> {
            Component[] pola = macierzPanel.getComponents();
            int rozmiar = (int) Math.sqrt(pola.length);

            // Wczytywanie danych z pól tekstowych
            double[][] macierz = new double[rozmiar][rozmiar];
            try {
                for (int i = 0; i < rozmiar; i++) {
                    for (int j = 0; j < rozmiar; j++) {
                        JTextField pole = (JTextField) pola[i * rozmiar + j];
                        macierz[i][j] = Double.parseDouble(pole.getText());
                    }
                }

                // Wybór metody obliczania
                String metoda = (String) metodaCombo.getSelectedItem();
                if ("Laplace".equals(metoda)) {
                    int indeks = (int) indeksCombo.getSelectedItem() - 1;
                    String wybor = (String) wyborLaplaceCombo.getSelectedItem();
                    if ("Wiersz".equalsIgnoreCase(wybor)) {
                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() {
                                pokazKrokiLaplace(macierz, true, indeks);
                                return null;
                            }

                            @Override
                            protected void done() {
                                double wynik = obliczWyznacznikLaplace(macierz, new JTextArea(), true, indeks);
                                wynikLabel.setText("Wyznacznik: " + formatujLiczbe(wynik));
                            }
                        };
                        worker.execute();
                    } else if ("Kolumna".equalsIgnoreCase(wybor)) {
                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() {
                                pokazKrokiLaplace(macierz, false, indeks);
                                return null;
                            }

                            @Override
                            protected void done() {
                                double wynik = obliczWyznacznikLaplace(macierz, new JTextArea(), false, indeks);
                                wynikLabel.setText("Wyznacznik: " + formatujLiczbe(wynik));
                            }
                        };
                        worker.execute();
                    }
                } else if ("Gauss".equals(metoda)) {
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            pokazKrokiGauss(macierz);
                            return null;
                        }

                        @Override
                        protected void done() {
                            double wynik = obliczWyznacznikGauss(macierz, new JTextArea());
                            wynikLabel.setText("Wyznacznik: " + formatujLiczbe(wynik));
                        }
                    };
                    worker.execute();
                } else if ("Sarrus".equals(metoda)) {
                    if (macierz.length != 3 || macierz[0].length != 3) {
                        JOptionPane.showMessageDialog(frame, "Metoda Sarrusa działa tylko dla macierzy 3x3!", "Błąd", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            String wybor = (String) wyborLaplaceCombo.getSelectedItem();
                            boolean dopiszWiersze = "Wiersz".equalsIgnoreCase(wybor);
                            pokazKrokiSarrus(macierz, dopiszWiersze);
                            return null;
                        }

                        @Override
                        protected void done() {
                            String wybor = (String) wyborLaplaceCombo.getSelectedItem();
                            boolean dopiszWiersze = "Wiersz".equalsIgnoreCase(wybor);
                            double wynik = obliczWyznacznikSarrus(macierz, new JTextArea(), dopiszWiersze);
                            wynikLabel.setText("Wyznacznik: " + formatujLiczbe(wynik));
                        }
                    };
                    worker.execute();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Wprowadź poprawne liczby w macierzy!", "Błąd", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });



        // Dolny panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Metoda: "));
        bottomPanel.add(metodaCombo);
        metodaCombo.addActionListener(e -> {
            int rozmiar = (int) Math.sqrt(macierzPanel.getComponentCount()); // Oblicz rozmiar macierzy
            boolean isLaplace = "Laplace".equals(metodaCombo.getSelectedItem());
            boolean isSarrus = "Sarrus".equals(metodaCombo.getSelectedItem());

            if (isSarrus) {
                if (rozmiar != 3) {
                    JOptionPane.showMessageDialog(frame, "Metoda Sarrusa działa tylko dla macierzy 3x3!", "Błąd", JOptionPane.ERROR_MESSAGE);
                    metodaCombo.setSelectedIndex(0); // Przywróć domyślną metodę (Laplace)
                } else {
                    rozmiarField.setEnabled(false); // Zablokuj zmianę rozmiaru macierzy
                }
            } else {
                rozmiarField.setEnabled(true); // Odblokuj zmianę rozmiaru dla innych metod
            }

            wyborLaplaceCombo.setEnabled(isLaplace || isSarrus);
            indeksCombo.setEnabled(isLaplace);
        });


        bottomPanel.add(new JLabel("Rozwinięcie: "));
        bottomPanel.add(wyborLaplaceCombo);
        bottomPanel.add(new JLabel("Indeks: "));
        bottomPanel.add(indeksCombo);
        bottomPanel.add(obliczPrzycisk);
        bottomPanel.add(wynikLabel);

        // Panel na sygnaturę
        JPanel sygnaturaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel sygnaturaLabel = new JLabel("© Olaf Olejnik, 122681, Społeczna Akademia Nauk, Łódź, Poland");
        sygnaturaPanel.add(sygnaturaLabel);

        // Kontener nadrzędny dla bottomPanel i sygnaturaPanel
        JPanel dolnyKontener = new JPanel();
        dolnyKontener.setLayout(new BoxLayout(dolnyKontener, BoxLayout.Y_AXIS));
        dolnyKontener.add(bottomPanel);
        dolnyKontener.add(sygnaturaPanel);

        // Dodawanie paneli do głównego okna
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(macierzPanel), BorderLayout.CENTER);
        panel.add(dolnyKontener, BorderLayout.SOUTH); // kontener nadrzędny z sygnaturą

        // Ustawienia okna
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    // Metoda pokazująca kroki dla metody Laplace'a
    private void pokazKrokiLaplace(double[][] macierz, boolean poWierszach, int indeks) {
        JTextArea krokiArea = new JTextArea(20, 50);
        krokiArea.setEditable(false);

        JFrame krokiFrame = new JFrame("Kroki - Laplace");
        krokiFrame.add(new JScrollPane(krokiArea));
        krokiFrame.setSize(600, 400);
        krokiFrame.setVisible(true);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                obliczWyznacznikLaplace(macierz, krokiArea, poWierszach, indeks);
                return null;
            }
        };

        worker.execute();
    }


    private double obliczWyznacznikLaplace(double[][] macierz, JTextArea krokiArea, boolean poWierszach, int indeks) {
        int n = macierz.length;

        if (n == 1) {
            krokiArea.append("Wyznacznik dla macierzy 1x1: " + macierz[0][0] + "\n");
            return macierz[0][0];
        } else if (n == 2) {
            BigDecimal bd1 = new BigDecimal(macierz[0][0]).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
            BigDecimal bd2 = new BigDecimal(macierz[1][1]).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
            BigDecimal bd3 = new BigDecimal(macierz[0][1]).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
            BigDecimal bd4 = new BigDecimal(macierz[1][0]).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();

// Obliczenie wyznacznika
            BigDecimal wynik = bd1.multiply(bd2).subtract(bd3.multiply(bd4));

// Wyświetlenie kroku
            krokiArea.append("Wyznacznik dla macierzy 2x2: " + wynik.stripTrailingZeros().toPlainString() + "\n");

// Zwrócenie wyniku jako double
            return wynik.doubleValue();
        }

        double wyznacznik = 0;
        for (int i = 0; i < n; i++) {
            int row = poWierszach ? indeks : i;
            int col = poWierszach ? i : indeks;
            if (macierz[row][col] == 0) {
                krokiArea.append("Element A(" + (row + 1) + "," + (col + 1) + ") = 0, pomijanie...\n");
                continue;
            }
            double[][] podmacierz = podmacierz(macierz, row, col);
            krokiArea.append("Obliczanie rozwinięcia dla elementu A(" + (row + 1) + "," + (col + 1) + "): " + macierz[row][col] + "\n");
            krokiArea.append("Podmacierz:\n");
            for (double[] wiersz : podmacierz) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int j = 0; j < wiersz.length; j++) {
                    BigDecimal bd = new BigDecimal(wiersz[j]).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
                    sb.append(bd.toPlainString());
                    if (j < wiersz.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
                krokiArea.append(sb.toString() + "\n");
            }

            double wynikCzesciowy = Math.pow(-1, row + col) * macierz[row][col] * obliczWyznacznikLaplace(podmacierz, krokiArea, true, 0);
            krokiArea.append("Częściowy wynik: " + formatujLiczbe(wynikCzesciowy) + "\n");
            wyznacznik += wynikCzesciowy;

        }
        krokiArea.append("Wyznacznik: " + formatujLiczbe(wyznacznik) + "\n");
        return wyznacznik;
    }

    // Metoda pokazująca kroki dla metody Gaussa
    private void pokazKrokiGauss(double[][] macierz) {
        JTextArea krokiArea = new JTextArea(20, 50);
        krokiArea.setEditable(false);

        JFrame krokiFrame = new JFrame("Kroki - Gauss");
        krokiFrame.add(new JScrollPane(krokiArea));
        krokiFrame.setSize(600, 400);
        krokiFrame.setVisible(true);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                obliczWyznacznikGauss(macierz, krokiArea);
                return null;
            }
        };

        worker.execute();
    }


    private double obliczWyznacznikGauss(double[][] macierz, JTextArea krokiArea) {
        int n = macierz.length;
        double[][] kopia = new double[n][n];

        // Kopiowanie macierzy
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                kopia[i][j] = macierz[i][j];
            }
        }

        double det = 1;
        for (int i = 0; i < n; i++) {
            // Znajdowanie maksymalnego elementu w kolumnie
            int max = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(kopia[j][i]) > Math.abs(kopia[max][i])) {
                    max = j;
                }
            }

            // Zamiana wierszy
            if (i != max) {
                double[] temp = kopia[i];
                kopia[i] = kopia[max];
                kopia[max] = temp;
                det *= -1; // Zmiana znaku wyznacznika przy zamianie wierszy
                appendToArea(krokiArea, "Zamiana: Wiersz " + (i + 1) + " <-> Wiersz " + (max + 1));
            }

            // Sprawdzanie zerowego elementu na przekątnej
            if (kopia[i][i] == 0) {
                appendToArea(krokiArea, "Wyznacznik równy 0 - zerowy element na przekątnej");
                return 0; // Wyznacznik zerowy
            }

            // Eliminacja Gaussa
            for (int j = i + 1; j < n; j++) {
                double factor = kopia[j][i] / kopia[i][i];
                appendToArea(krokiArea, "Wiersz " + (j + 1) + " = Wiersz " + (j + 1) + " - " + factor + " * Wiersz " + (i + 1));
                for (int k = i; k < n; k++) {
                    kopia[j][k] -= factor * kopia[i][k];
                }
            }

            det *= kopia[i][i];
        }

        appendToArea(krokiArea, "Wyznacznik: " + formatujLiczbe(det));
        return det;
    }


    //Metoda pokazująca kroki dla metody Sarrusa
    private void pokazKrokiSarrus(double[][] macierz, boolean dopiszWiersze) {
        JTextArea krokiArea = new JTextArea(20, 50);
        krokiArea.setEditable(false);

        JFrame krokiFrame = new JFrame("Kroki - Sarrus");
        krokiFrame.add(new JScrollPane(krokiArea));
        krokiFrame.setSize(600, 400);
        krokiFrame.setVisible(true);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                obliczWyznacznikSarrus(macierz, krokiArea, dopiszWiersze);
                return null;
            }
        };

        worker.execute();
    }
    private double obliczWyznacznikSarrus(double[][] macierz, JTextArea krokiArea, boolean dopiszWiersze) {
        if (macierz.length != 3 || macierz[0].length != 3) {
            throw new IllegalArgumentException("Metoda Sarrusa działa tylko dla macierzy 3x3.");
        }

        double[][] rozszerzona = new double[3][5];

        if (dopiszWiersze) {
            // Dopisywanie dwóch pierwszych wierszy
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rozszerzona[i][j] = macierz[i][j];
                    if (j < 2) {
                        rozszerzona[i][j + 3] = macierz[i][j]; // Kopiowanie dodatkowych kolumn
                    }
                }
            }
        } else {
            // Dopisywanie dwóch pierwszych kolumn
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rozszerzona[i][j] = macierz[i][j];
                }
            }
            for (int i = 0; i < 3; i++) {
                rozszerzona[i][3] = macierz[i][0]; // Kopiowanie pierwszej kolumny
                rozszerzona[i][4] = macierz[i][1]; // Kopiowanie drugiej kolumny
            }
        }

        // Obliczanie przekątnych
        double suma1 = rozszerzona[0][0] * rozszerzona[1][1] * rozszerzona[2][2] +
                rozszerzona[0][1] * rozszerzona[1][2] * rozszerzona[2][3] +
                rozszerzona[0][2] * rozszerzona[1][3] * rozszerzona[2][4];
        krokiArea.append("Suma iloczynów przekątnych: " + suma1 + "\n");

        double suma2 = rozszerzona[0][4] * rozszerzona[1][3] * rozszerzona[2][2] +
                rozszerzona[0][3] * rozszerzona[1][2] * rozszerzona[2][1] +
                rozszerzona[0][2] * rozszerzona[1][1] * rozszerzona[2][0];
        krokiArea.append("Suma iloczynów przeciwprzekątnych: " + suma2 + "\n");

        double wynik = suma1 - suma2;
        krokiArea.append("Wyznacznik (Suma1 - Suma2): " + formatujLiczbe(wynik) + "\n");
        return wynik;
    }


    // Metoda pomocnicza do aktualizacji JTextArea w czasie rzeczywistym
    private void appendToArea(JTextArea krokiArea, String message) {
        SwingUtilities.invokeLater(() -> krokiArea.append(message + "\n"));
    }

    // Metoda pomocnicza niwelująca problemy z wyświetlaniem liczb
    private String formatujLiczbe(double liczba) {
        BigDecimal bd = new BigDecimal(liczba);
        bd = bd.setScale(5, RoundingMode.HALF_UP); // Zaokrąglij do 5 miejsc
        return bd.stripTrailingZeros().toPlainString(); // Usuń zbędne zera i .0
    }

    // Metoda do tworzenia podmacierzy (pomocnicza do obliczania wyznacznika)
    private double[][] podmacierz(double[][] macierz, int wiersz, int kolumna) {
        int n = macierz.length;
        double[][] podmacierz = new double[n - 1][n - 1];

        int p = 0;
        for (int i = 0; i < n; i++) {
            if (i == wiersz) continue;
            int q = 0;
            for (int j = 0; j < n; j++) {
                if (j == kolumna) continue;
                podmacierz[p][q] = macierz[i][j];
                q++;
            }
            p++;
        }
        return podmacierz;
    }

}
