package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.models.Content;
import edu.ufp.streaming.rec.models.Genre;

import java.io.*;

/**
 * Classe responsável pela serialização e deserialização binária do estado do sistema.
 * Permite exportar e importar os gestores de conteúdos e géneros em ficheiros binários.
 *
 * @author Pedro
 */
public class ContentSerializer {

    /**
     * Exporta o ContentManager para um ficheiro binário.
     *
     * @param cm       gestor de conteúdos a exportar
     * @param filePath caminho do ficheiro de destino
     */
    public static void exportContents(ContentManager cm, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(cm.listAll());
            System.out.println("Conteúdos serializados para " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao serializar conteúdos: " + e.getMessage());
        }
    }

    /**
     * Importa conteúdos de um ficheiro binário para o ContentManager.
     *
     * @param cm       gestor de conteúdos de destino
     * @param filePath caminho do ficheiro de origem
     */
    @SuppressWarnings("unchecked")
    public static void importContents(ContentManager cm, String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            java.util.List<Content> list = (java.util.List<Content>) ois.readObject();
            for (Content c : list) {
                cm.insert(c);
            }
            System.out.println("Conteúdos deserializados de " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao deserializar conteúdos: " + e.getMessage());
        }
    }

    /**
     * Exporta o GenreManager para um ficheiro binário.
     *
     * @param gm       gestor de géneros a exportar
     * @param filePath caminho do ficheiro de destino
     */
    public static void exportGenres(GenreManager gm, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gm.listAll());
            System.out.println("Géneros serializados para " + filePath);
        } catch (IOException e) {
            System.err.println("Erro ao serializar géneros: " + e.getMessage());
        }
    }

    /**
     * Importa géneros de um ficheiro binário para o GenreManager.
     *
     * @param gm       gestor de géneros de destino
     * @param filePath caminho do ficheiro de origem
     */
    @SuppressWarnings("unchecked")
    public static void importGenres(GenreManager gm, String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            java.util.List<Genre> list = (java.util.List<Genre>) ois.readObject();
            for (Genre g : list) {
                gm.insert(g);
            }
            System.out.println("Géneros deserializados de " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao deserializar géneros: " + e.getMessage());
        }
    }
}