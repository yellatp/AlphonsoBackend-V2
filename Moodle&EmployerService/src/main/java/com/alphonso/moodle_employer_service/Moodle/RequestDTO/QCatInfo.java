package com.alphonso.moodle_employer_service.Moodle.RequestDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QCatInfo(Long id, String name, Long parent, Long contextid) { }