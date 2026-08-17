package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

class PdfFieldOverlayFillerVisualCheckTest {

    @Test
    void preencheOPdfEExportaImagemParaInspecaoVisual() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("termo_adesao_prestamista.pdf");
        if (inputStream == null) {
            throw new IllegalStateException("Template não encontrado no classpath");
        }
        byte[] templateBytes;
        try (inputStream) {
            templateBytes = inputStream.readAllBytes();
        }

        Map<String, String> fieldValues = new LinkedHashMap<>();
        fieldValues.put("SEGURADO", "Maria da Silva Teste");
        fieldValues.put("CPF/CNPJ", "123.456.789-00");
        fieldValues.put("DATA DE NASCIMENTO", "15/03/1985");
        fieldValues.put("ENDEREÇO", "Rua das Flores");
        fieldValues.put("NÚMERO", "123");
        fieldValues.put("COMPLEMENTO", "Apto 45");
        fieldValues.put("BAIRRO", "Centro");
        fieldValues.put("CEP", "01234-567");
        fieldValues.put("CIDADE", "São Paulo");
        fieldValues.put("UF", "SP");
        fieldValues.put("N° CONTRATO", "999888777");
        fieldValues.put("DDD / CELULAR", "(11) 99999-8888");
        fieldValues.put("E-MAIL", "maria.teste@example.com");
        fieldValues.put("INÍCIO DE VIGÊNCIA", "01/08/2026");
        fieldValues.put("FIM DE VIGÊNCIA", "01/08/2027");
        fieldValues.put("N° APÓLICE", "APL-000123");
        fieldValues.put("PRÊMIO LÍQUIDO TOTAL (R$)", "146,50");
        fieldValues.put("IOF (R$)", "3,50");
        fieldValues.put("PRÊMIO BRUTO TOTAL (R$)", "150,00");

        byte[] filledPdf = PdfFieldOverlayFiller.fill(templateBytes, fieldValues);
        assertTrue(filledPdf.length > 0, "PDF preenchido não deveria estar vazio");

        File outputDir = new File("build/pdf-visual-check");
        outputDir.mkdirs();
        java.nio.file.Files.write(new File(outputDir, "termo_adesao_preenchido.pdf").toPath(), filledPdf);

        try (PDDocument document = Loader.loadPDF(filledPdf)) {
            PDFRenderer renderer = new PDFRenderer(document);
            var image = renderer.renderImageWithDPI(0, 150f, ImageType.RGB);
            ImageIO.write(image, "png", new File(outputDir, "termo_adesao_pagina1.png"));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
