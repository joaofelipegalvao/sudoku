package br.com.dio;

import br.com.dio.gui.SudokuGUI;
import br.com.dio.model.Board;
import br.com.dio.model.Space;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import static br.com.dio.util.BoardTemplate.BOARD_TEMPLATE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class Main {

   private final static Scanner scanner = new Scanner(System.in);
   private static Board board;
   private final static int BOARD_LIMIT = 9;

   public static void main(String[] args) {
      System.out.println("=== SUDOKU - DIO CHALLENGE ===");
      System.out.println("Escolha o modo de jogo:");
      System.out.println("1 - Interface Gráfica (GUI)");
      System.out.println("2 - Terminal/Console");
      System.out.print("Opção: ");

      Scanner modeScanner = new Scanner(System.in);
      int mode = -1;

      try {
         mode = modeScanner.nextInt();
      } catch (Exception e) {
         System.out.println("Opção inválida! Iniciando modo GUI...");
         mode = 1;
      }

      switch (mode) {
         case 1 -> startGUIMode();
         case 2 -> startConsoleMode(args);
         default -> {
            System.out.println("Opção inválida! Iniciando modo GUI...");
            startGUIMode();
         }
      }
   }

   private static void startGUIMode() {
      System.out.println("Iniciando modo gráfico...");
      SwingUtilities.invokeLater(() -> {
         try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
            try {
               UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ex) {
               try {
                  UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
               } catch (Exception exc) {
                  exc.printStackTrace();
               }
            }
         }
         new SudokuGUI();
      });
   }

   private static void startConsoleMode(String[] args) {
      System.out.println("Iniciando modo console...");
      final var positions = Stream.of(args)
              .collect(toMap(
                      k -> k.split(";")[0],
                      v -> v.split(";")[1]
              ));

      var option = -1;
      while (true) {
         System.out.println("\n=== MENU SUDOKU ===");
         System.out.println("Selecione uma das opções a seguir");
         System.out.println("1 - Iniciar um novo Jogo");
         System.out.println("2 - Colocar um novo número");
         System.out.println("3 - Remover um número");
         System.out.println("4 - Visualizar jogo atual");
         System.out.println("5 - Verificar status do jogo");
         System.out.println("6 - Limpar jogo");
         System.out.println("7 - Finalizar jogo");
         System.out.println("8 - Sair");
         System.out.print("Opção: ");

         try {
            option = scanner.nextInt();
         } catch (Exception e) {
            System.out.println("Entrada inválida! Digite um número.");
            scanner.nextLine(); // Limpar buffer
            continue;
         }

         switch (option) {
            case 1 -> startGame(positions);
            case 2 -> inputNumber();
            case 3 -> removeNumber();
            case 4 -> showCurrentGame();
            case 5 -> showGameStatus();
            case 6 -> clearGame();
            case 7 -> finishGame();
            case 8 -> {
               System.out.println("Obrigado por jogar!");
               System.exit(0);
            }
            default -> System.out.println("Opção inválida, selecione uma das opções do menu");
         }
      }
   }

   private static void startGame(final Map<String, String> positions) {
      if (nonNull(board)) {
         System.out.println("⚠️  O jogo já foi iniciado");
         return;
      }

      List<List<Space>> spaces = new ArrayList<>();
      for (int i = 0; i < BOARD_LIMIT; i++) {
         spaces.add(new ArrayList<>());
         for (int j = 0; j < BOARD_LIMIT; j++) {
            var positionConfig = positions.getOrDefault("%s,%s".formatted(i, j), "0,false");
            var expected = Integer.parseInt(positionConfig.split(",")[0]);
            var fixed = Boolean.parseBoolean(positionConfig.split(",")[1]);
            var currentSpace = new Space(expected, fixed);
            spaces.get(i).add(currentSpace);
         }
      }

      board = new Board(spaces);
      System.out.println("✅ O jogo está pronto para começar");
      showCurrentGame();
   }

   private static void inputNumber() {
      if (isNull(board)) {
         System.out.println("⚠️  O jogo ainda não foi iniciado");
         return;
      }

      System.out.println("📍 Informe a coluna em que o número será inserido (0-8):");
      var col = runUntilGetValidNumber(0, 8);
      System.out.println("📍 Informe a linha em que o número será inserido (0-8):");
      var row = runUntilGetValidNumber(0, 8);
      System.out.printf("🔢 Informe o número que vai entrar na posição [%s,%s] (1-9):\n", col, row);
      var value = runUntilGetValidNumber(1, 9);

      if (!board.changeValue(col, row, value)) {
         System.out.printf("🔒 A posição [%s,%s] tem um valor fixo\n", col, row);
      } else {
         System.out.printf("✅ Número %d inserido na posição [%s,%s]\n", value, col, row);
      }
   }

   private static void removeNumber() {
      if (isNull(board)) {
         System.out.println("⚠️  O jogo ainda não foi iniciado");
         return;
      }

      System.out.println("📍 Informe a coluna do número a ser removido (0-8):");
      var col = runUntilGetValidNumber(0, 8);
      System.out.println("📍 Informe a linha do número a ser removido (0-8):");
      var row = runUntilGetValidNumber(0, 8);

      if (!board.clearValue(col, row)) {
         System.out.printf("🔒 A posição [%s,%s] tem um valor fixo\n", col, row);
      } else {
         System.out.printf("✅ Número removido da posição [%s,%s]\n", col, row);
      }
   }

   private static void showCurrentGame() {
      if (isNull(board)) {
         System.out.println("⚠️  O jogo ainda não foi iniciado");
         return;
      }

      var args = new Object[81];
      var argPos = 0;
      for (int i = 0; i < BOARD_LIMIT; i++) {
         for (var col : board.getSpaces()) {
            args[argPos++] = " " + ((isNull(col.get(i).getActual())) ? " " : col.get(i).getActual());
         }
      }
      System.out.println("\n🎮 Seu jogo se encontra da seguinte forma:");
      System.out.printf((BOARD_TEMPLATE) + "\n", args);
   }

   private static void showGameStatus() {
      if (isNull(board)) {
         System.out.println("⚠️  O jogo ainda não foi iniciado");
         return;
      }

      System.out.printf("📊 O jogo atualmente se encontra no status: %s\n", board.getStatus().getLabel());
      if (board.hasErrors()) {
         System.out.println("❌ O jogo contém erros");
      } else {
         System.out.println("✅ O jogo não contém erros");
      }
   }

   private static void clearGame() {
      if (isNull(board)) {
         System.out.println("⚠️  O jogo ainda não foi iniciado");
         return;
      }

      System.out.println("🗑️  Tem certeza que deseja limpar seu jogo e perder todo seu progresso? (sim/não)");
      var confirm = scanner.next();
      while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não") &&
              !confirm.equalsIgnoreCase("nao") && !confirm.equalsIgnoreCase("s") &&
              !confirm.equalsIgnoreCase("n")) {
         System.out.println("Por favor, informe 'sim' ou 'não':");
         confirm = scanner.next();
      }

      if (confirm.equalsIgnoreCase("sim") || confirm.equalsIgnoreCase("s")) {
         board.reset();
         System.out.println("🧹 Jogo limpo com sucesso!");
         showCurrentGame();
      } else {
         System.out.println("❌ Operação cancelada");
      }
   }

   private static void finishGame() {
      if (isNull(board)) {
         System.out.println("⚠️  O jogo ainda não foi iniciado");
         return;
      }

      if (board.gameIsFinished()) {
         System.out.println("🎉 PARABÉNS! Você concluiu o jogo com sucesso!");
         showCurrentGame();
         System.out.println("🏆 Obrigado por jogar!");
         board = null;
      } else if (board.hasErrors()) {
         System.out.println("❌ Seu jogo contém erros, verifique seu board e ajuste-o");
         showGameStatus();
      } else {
         System.out.println("⏳ Você ainda precisa preencher alguns espaços");
         showGameStatus();
      }
   }

   private static int runUntilGetValidNumber(final int min, final int max) {
      int current;
      while (true) {
         try {
            current = scanner.nextInt();
            if (current >= min && current <= max) {
               break;
            } else {
               System.out.printf("⚠️  Informe um número entre %s e %s:\n", min, max);
            }
         } catch (Exception e) {
            System.out.printf("⚠️  Entrada inválida! Informe um número entre %s e %s:\n", min, max);
            scanner.nextLine(); // Limpar buffer
         }
      }
      return current;
   }
}