package br.psi.giganet.api.purchase.common.reports.csv.services;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CsvReportService {

    public File csvWriterAll(String[] headers, List<String[]> data, File file) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(file));
        writer.writeNext(headers);
        writer.writeAll(data);
        writer.close();
        return file;
    }

    public File getPurchaseOrdersListReportAsCsv(
            List<Map<String, Object>> orders,
            LocalDate initialDate,
            LocalDate finalDate,
            ProcessStatus... statuses
    ) throws Exception {

        File file = File.createTempFile("ordens_de_compra_" + initialDate.toString() + "_" + finalDate.toString(), ".csv");
        return csvWriterAll(
                new String[]{"Data", "Código", "Fornecedor", "Situação", "Descrição", "Centro de Custo", "Total", "Condição de Pagamento"},
                orders.stream()
                        .map(order -> order.keySet().stream()
                                .sorted((key1, key2) -> {
                                    Map<String, Integer> positions = new HashMap<>();
                                    positions.put("date", 0);
                                    positions.put("id", 1);
                                    positions.put("supplier", 2);
                                    positions.put("status", 3);
                                    positions.put("description", 4);
                                    positions.put("costCenter", 5);
                                    positions.put("total", 6);
                                    positions.put("paymentCondition", 7);

                                    return positions.get(key1) - positions.get(key2);
                                })
                                .map(key -> {
                                    Object value = order.get(key);
                                    if (key.equals("date")) {
                                        return LocalDateTime.parse(value.toString().replaceAll(" ", "T"))
                                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                    } else if (key.equals("status")) {
                                        switch (ProcessStatus.valueOf(value.toString())) {
                                            case PENDING:
                                                return "Pendente";
                                            case REALIZED:
                                                return "Realizada";
                                            case IN_TRANSIT:
                                                return "Em Trânsito";
                                            case PARTIALLY_RECEIVED:
                                                return "Parcialmente Recebida";
                                            case RECEIVED:
                                                return "Recebida";
                                            case FINALIZED:
                                                return "Finalizada";
                                            case CANCELED:
                                                return "Cancelada";
                                            default:
                                                return "-";
                                        }

                                    } else {
                                        return value == null ? "-" : value.toString();
                                    }
                                })
                                .toArray(String[]::new))
                        .collect(Collectors.toList()),
                file);
    }
}
