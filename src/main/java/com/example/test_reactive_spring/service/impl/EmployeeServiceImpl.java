package com.example.test_reactive_spring.service.impl;

import com.example.test_reactive_spring.dao.EmployeeDao;
import com.example.test_reactive_spring.entity.Employee;
import com.example.test_reactive_spring.service.EmployeeService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeDao employeeDao;

    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

    @Value("${reports.download.query}")
    private String reportsQuery;

    @Override
    public Flux<Employee> getAll() {
        return employeeDao.findAll();
    }

    @Override
    public Mono<Employee> getById(Long empId) {
        return employeeDao.findById(empId);
    }

    @Override
    public Flux<DataBuffer> exportEmployeesToExcel() {
        return employeeDao.findAll()
                .collectList() // Collect all employees into a single list
                .flatMapMany(employeeList ->
                        // Offload the blocking work to a separate thread
                        Mono.fromCallable(() -> createExcelFile(employeeList))
                                .subscribeOn(Schedulers.boundedElastic())
                ).flatMap(fluxOfDataBuffer -> fluxOfDataBuffer);
    }

    private Flux<DataBuffer> createExcelFile(List<Employee> employeeList) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Employees");
            writeHeader(sheet);

            AtomicInteger rowCount = new AtomicInteger(1);
            employeeList.forEach(employee -> writeRow(sheet, rowCount.getAndIncrement(), employee));

            workbook.write(outputStream);

            // Return the created file as a Flux of a single DataBuffer
            return Flux.just(bufferFactory.wrap(outputStream.toByteArray()));

        } catch (IOException e) {
            return Flux.error(new RuntimeException("Failed to generate Excel file", e));
        }
    }

    private void writeHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Emp ID");
        headerRow.createCell(1).setCellValue("First Name");
        headerRow.createCell(2).setCellValue("Last Name");
        headerRow.createCell(3).setCellValue("Country");
    }

    private void writeRow(Sheet sheet, int rowIndex, Employee employee) {
        Row dataRow = sheet.createRow(rowIndex);
        dataRow.createCell(0).setCellValue(employee.getEmpId());
        dataRow.createCell(1).setCellValue(employee.getFirstName());
        dataRow.createCell(2).setCellValue(employee.getLastName());
        dataRow.createCell(3).setCellValue(employee.getCtryCd());
    }
}
