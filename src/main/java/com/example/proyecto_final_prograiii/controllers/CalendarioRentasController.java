package com.example.proyecto_final_prograiii.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.example.proyecto_final_prograiii.DAO.AlquilerDAO;
import com.example.proyecto_final_prograiii.models.Alquiler;
import javafx.fxml.FXML;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class CalendarioRentasController {
    @FXML
    private CalendarView calendarView;

    private final Calendar calendarioRentas = new Calendar("Rentas");

    @FXML
    public void initialize() {
        calendarioRentas.setStyle(Calendar.Style.STYLE1); // azul (puedes cambiarlo)

        CalendarSource source = new CalendarSource("FuenteRentals");
        source.getCalendars().add(calendarioRentas);
        calendarView.getCalendarSources().setAll(source);

        cargarEventos();
    }

    AlquilerDAO alquilerDAO = new AlquilerDAO();
    private void cargarEventos() {
        List<Map<String, Object>> eventos = alquilerDAO.obtenerEventosCalendario();

        Calendar calendar = new Calendar("Alquileres");

        for (Map<String, Object> e : eventos) {

            Entry<String> entry = new Entry<>((String) e.get("titulo"));

            LocalDate inicio = (LocalDate) e.get("inicio");
            LocalDate fin = (LocalDate) e.get("fin");

            entry.setInterval(
                    inicio.atStartOfDay(),
                    fin.plusDays(1).atStartOfDay()
            );

            calendar.addEntry(entry);
        }

        calendarView.getCalendarSources().add(new CalendarSource("Source") {{
            getCalendars().add(calendar);
        }});

    }
}
