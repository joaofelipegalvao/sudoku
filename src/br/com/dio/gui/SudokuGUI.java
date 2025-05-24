package br.com.dio.gui;

import br.com.dio.model.Board;
import br.com.dio.model.Space;

import javax.swing.*;
import java.awt.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class SudokuGUI extends JFrame {

   private static final int BOARD_SIZE = 9;
   private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
   private static final Color FIXED_CELL_COLOR = new Color(200, 200, 200);
   private static final Color NORMAL_CELL_COLOR = Color.WHITE;
   private static final Color ERROR_CELL_COLOR = new Color(255, 200, 200);
   private static final Color BORDER_COLOR = new Color(100, 100, 100);

   private Board board;
   private JTextField[][] cells;
   private JLabel statusLabel;
   private JButton newGameButton;
   private JButton checkButton;
   private JButton clearButton;
   private JButton finishButton;

   public SudokuGUI() {
      initializeGUI();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
   }

   private void initializeGUI() {
      setTitle("Sudoku - DIO Challenge");
      setLayout(new BorderLayout());

      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.setBackground(BACKGROUND_COLOR);
      mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      JPanel gridPanel = createSudokuGrid();
      mainPanel.add(gridPanel, BorderLayout.CENTER);

      JPanel controlPanel = createControlPanel();
      mainPanel.add(controlPanel, BorderLayout.SOUTH);

      JPanel statusPanel = createStatusPanel();
      mainPanel.add(statusPanel, BorderLayout.NORTH);

      add(mainPanel);
   }

   private JPanel createSudokuGrid() {
      JPanel gridPanel = new JPanel(new GridLayout(3, 3, 3, 3));
      gridPanel.setBackground(BORDER_COLOR);
      gridPanel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createRaisedBevelBorder(),
              BorderFactory.createEmptyBorder(5, 5, 5, 5)
      ));

      cells = new JTextField[BOARD_SIZE][BOARD_SIZE];

      // Criar os 9 sub-grids (3x3)
      for (int blockRow = 0; blockRow < 3; blockRow++) {
         for (int blockCol = 0; blockCol < 3; blockCol++) {
            JPanel subGrid = new JPanel(new GridLayout(3, 3, 1, 1));
            subGrid.setBackground(BORDER_COLOR);
            subGrid.setBorder(BorderFactory.createLoweredBevelBorder());

            // Preencher cada sub-grid
            for (int row = 0; row < 3; row++) {
               for (int col = 0; col < 3; col++) {
                  int actualRow = blockRow * 3 + row;
                  int actualCol = blockCol * 3 + col;

                  JTextField cell = new JTextField();
                  cell.setHorizontalAlignment(JTextField.CENTER);
                  cell.setFont(new Font("Arial", Font.BOLD, 18));
                  cell.setPreferredSize(new Dimension(45, 45));
                  cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                  final int r = actualRow;
                  final int c = actualCol;

                  cell.addKeyListener(new KeyAdapter() {
                     @Override
                     public void keyTyped(KeyEvent e) {
                        char ch = e.getKeyChar();
                        if (!Character.isDigit(ch) || ch == '0' || cell.getText().length() >= 1) {
                           e.consume();
                        }
                     }

                     @Override
                     public void keyReleased(KeyEvent e) {
                        updateBoardFromGUI();
                        updateCellColors();
                     }
                  });

                  cells[actualRow][actualCol] = cell;
                  subGrid.add(cell);
               }
            }

            gridPanel.add(subGrid);
         }
      }

      return gridPanel;
   }

   private JPanel createControlPanel() {
      JPanel controlPanel = new JPanel(new FlowLayout());
      controlPanel.setBackground(BACKGROUND_COLOR);

      newGameButton = new JButton("Novo Jogo");
      newGameButton.setFont(new Font("Arial", Font.BOLD, 12));
      newGameButton.addActionListener(e -> startNewGame());

      checkButton = new JButton("Verificar");
      checkButton.setFont(new Font("Arial", Font.BOLD, 12));
      checkButton.addActionListener(e -> checkGame());
      checkButton.setEnabled(false);

      clearButton = new JButton("Limpar");
      clearButton.setFont(new Font("Arial", Font.BOLD, 12));
      clearButton.addActionListener(e -> clearGame());
      clearButton.setEnabled(false);

      finishButton = new JButton("Finalizar");
      finishButton.setFont(new Font("Arial", Font.BOLD, 12));
      finishButton.addActionListener(e -> finishGame());
      finishButton.setEnabled(false);

      controlPanel.add(newGameButton);
      controlPanel.add(checkButton);
      controlPanel.add(clearButton);
      controlPanel.add(finishButton);

      return controlPanel;
   }

   private JPanel createStatusPanel() {
      JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      statusPanel.setBackground(BACKGROUND_COLOR);

      statusLabel = new JLabel("Clique em 'Novo Jogo' para começar");
      statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
      statusLabel.setForeground(new Color(60, 60, 60));

      statusPanel.add(statusLabel);

      return statusPanel;
   }

   private void startNewGame() {
      // Configuração padrão de um Sudoku simples
      Map<String, String> positions = getDefaultSudokuConfiguration();

      List<List<Space>> spaces = new ArrayList<>();
      for (int i = 0; i < BOARD_SIZE; i++) {
         spaces.add(new ArrayList<>());
         for (int j = 0; j < BOARD_SIZE; j++) {
            var positionConfig = positions.getOrDefault("%s,%s".formatted(i, j), "0,false");
            var expected = Integer.parseInt(positionConfig.split(",")[0]);
            var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
            var currentSpace = new Space(expected, fixed);
            spaces.get(i).add(currentSpace);
         }
      }

      board = new Board(spaces);
      updateGUIFromBoard();

      checkButton.setEnabled(true);
      clearButton.setEnabled(true);
      finishButton.setEnabled(true);

      statusLabel.setText("Jogo iniciado! Status: " + board.getStatus().getLabel());
   }

   private Map<String, String> getDefaultSudokuConfiguration() {
      String[] config = {
              "0,0;5,true", "0,1;3,true", "0,4;7,true",
              "1,0;6,true", "1,3;1,true", "1,4;9,true", "1,5;5,true",
              "2,1;9,true", "2,2;8,true", "2,7;6,true",
              "3,0;8,true", "3,4;6,true", "3,8;3,true",
              "4,0;4,true", "4,3;8,true", "4,5;3,true", "4,8;1,true",
              "5,0;7,true", "5,4;2,true", "5,8;6,true",
              "6,1;6,true", "6,6;2,true", "6,7;8,true",
              "7,3;4,true", "7,4;1,true", "7,5;9,true", "7,8;5,true",
              "8,4;8,true", "8,7;7,true", "8,8;9,true"
      };

      return Stream.of(config)
              .collect(toMap(
                      k -> k.split(";")[0],
                      v -> v.split(";")[1]
              ));
   }

   private void updateGUIFromBoard() {
      if (isNull(board)) return;

      var spaces = board.getSpaces();
      for (int i = 0; i < BOARD_SIZE; i++) {
         for (int j = 0; j < BOARD_SIZE; j++) {
            Space space = spaces.get(i).get(j);
            JTextField cell = cells[i][j];

            if (space.isFixed()) {
               cell.setText(String.valueOf(space.getActual()));
               cell.setEditable(false);
               cell.setBackground(FIXED_CELL_COLOR);
               cell.setFont(new Font("Arial", Font.BOLD, 18));
            } else {
               cell.setText(nonNull(space.getActual()) ? String.valueOf(space.getActual()) : "");
               cell.setEditable(true);
               cell.setBackground(NORMAL_CELL_COLOR);
               cell.setFont(new Font("Arial", Font.PLAIN, 18));
            }
         }
      }

      updateCellColors();
   }

   private void updateBoardFromGUI() {
      if (isNull(board)) return;

      for (int i = 0; i < BOARD_SIZE; i++) {
         for (int j = 0; j < BOARD_SIZE; j++) {
            JTextField cell = cells[i][j];
            String text = cell.getText().trim();

            if (!text.isEmpty() && Character.isDigit(text.charAt(0))) {
               int value = Character.getNumericValue(text.charAt(0));
               board.changeValue(i, j, value);
            } else {
               board.clearValue(i, j);
            }
         }
      }
   }

   private void updateCellColors() {
      if (isNull(board)) return;

      var spaces = board.getSpaces();
      for (int i = 0; i < BOARD_SIZE; i++) {
         for (int j = 0; j < BOARD_SIZE; j++) {
            Space space = spaces.get(i).get(j);
            JTextField cell = cells[i][j];

            if (!space.isFixed()) {
               if (nonNull(space.getActual()) && !space.getActual().equals(space.getExpected())) {
                  cell.setBackground(ERROR_CELL_COLOR);
               } else {
                  cell.setBackground(NORMAL_CELL_COLOR);
               }
            }
         }
      }
   }

   private void checkGame() {
      if (isNull(board)) return;

      updateBoardFromGUI();

      String status = "Status: " + board.getStatus().getLabel();
      if (board.hasErrors()) {
         status += " - Contém erros!";
         statusLabel.setForeground(Color.RED);
      } else {
         status += " - Sem erros!";
         statusLabel.setForeground(new Color(0, 150, 0));
      }

      statusLabel.setText(status);
      updateCellColors();
   }

   private void clearGame() {
      if (isNull(board)) return;

      int result = JOptionPane.showConfirmDialog(
              this,
              "Tem certeza que deseja limpar o jogo e perder todo o progresso?",
              "Confirmar Limpeza",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE
      );

      if (result == JOptionPane.YES_OPTION) {
         board.reset();
         updateGUIFromBoard();
         statusLabel.setText("Jogo limpo! Status: " + board.getStatus().getLabel());
         statusLabel.setForeground(new Color(60, 60, 60));
      }
   }

   private void finishGame() {
      if (isNull(board)) return;

      updateBoardFromGUI();

      if (board.gameIsFinished()) {
         JOptionPane.showMessageDialog(
                 this,
                 "Parabéns! Você concluiu o Sudoku com sucesso!",
                 "Jogo Concluído",
                 JOptionPane.INFORMATION_MESSAGE
         );

         statusLabel.setText("Jogo concluído com sucesso!");
         statusLabel.setForeground(new Color(0, 150, 0));

         // Desabilitar controles
         checkButton.setEnabled(false);
         clearButton.setEnabled(false);
         finishButton.setEnabled(false);

      } else if (board.hasErrors()) {
         JOptionPane.showMessageDialog(
                 this,
                 "Seu jogo contém erros. Verifique as células em vermelho e corrija-as.",
                 "Jogo com Erros",
                 JOptionPane.ERROR_MESSAGE
         );

         statusLabel.setText("Jogo contém erros - corrija as células em vermelho");
         statusLabel.setForeground(Color.RED);
         updateCellColors();

      } else {
         JOptionPane.showMessageDialog(
                 this,
                 "Você ainda precisa preencher alguns espaços para completar o jogo.",
                 "Jogo Incompleto",
                 JOptionPane.WARNING_MESSAGE
         );

         statusLabel.setText("Jogo incompleto - preencha todos os espaços");
         statusLabel.setForeground(new Color(200, 150, 0));
      }
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> {
         try {
            // Primeiro tenta Nimbus (mais bonito)
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (Exception e) {
            try {
               // Fallback para sistema
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
               try {
                  // Último fallback
                  UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
               } catch (Exception exc) {
                  exc.printStackTrace();
               }
            }
         }
         new SudokuGUI();
      });
   }
}