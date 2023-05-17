package com.hyutils.core;

import java.util.ArrayList;
import java.util.List;

public class HasAttributes {

    /**
     * The model's attributes.
     *
     * @var array
     */
    protected List<String> attributes = new ArrayList<>();

    /**
     * The model attribute's original state.
     *
     * @var array
     */
    protected List<String> original = new ArrayList<>();

    /**
     * The changed model attributes.
     *
     * @var array
     */
    protected List<String> changes = new ArrayList<>();

    /**
     * The attributes that should be cast to native types.
     *
     * @var array
     */
    protected List<String> casts = new ArrayList<>();

    /**
     * The attributes that should be mutated to dates.
     *
     * @var array
     */
    protected List<String> dates = new ArrayList<>();

    /**
     * The storage format of the model's date columns.
     *
     * @var string
     */
    protected List<String> dateFormat;

    /**
     * The accessors to append to the model's array form.
     *
     * @var array
     */
    protected List<String> appends = new ArrayList<>();

    /**
     * Indicates whether attributes are snake cased on arrays.
     *
     * @var bool
     */
    public Boolean snakeAttributes = true;

    /**
     * The cache of the mutated attributes for each class.
     *
     * @var array
     */
    protected static List<String> mutatorCache = new ArrayList<>();



}
