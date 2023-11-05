package com.bootiful;

import org.springframework.data.annotation.Id;

record Customer(@Id Integer id, String name) {

}