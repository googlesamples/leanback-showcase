#!/usr/bin/env python

# Copyright 2016 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import json
import random
import webapp2
import time
from google.appengine.ext import ndb


# category data base model
class Category(ndb.Model):
    category = ndb.StringProperty(indexed=True, required=True)


# Movie model in detailed view page
class MovieClip(ndb.Model):
    id = ndb.IntegerProperty(indexed=True, required=True)
    category = ndb.StringProperty(indexed=True, required=True)
    description = ndb.StringProperty(indexed=True, required=True)
    source = ndb.StringProperty(indexed=False, required=True)
    card = ndb.StringProperty(indexed=False, required=True)
    background = ndb.StringProperty(indexed=False, required=True)
    title = ndb.StringProperty(indexed=False, required=True)
    studio = ndb.StringProperty(indexed=False, required=True)
    rented = ndb.BooleanProperty(indexed=False, required=True)


# movie model in overview page
class MovieOverview(ndb.Model):
    id = ndb.IntegerProperty(indexed=True, required=True)
    category = ndb.StringProperty(indexed=True, required=True)
    source = ndb.StringProperty(indexed=False, required=True)
    card = ndb.StringProperty(indexed=False, required=True)
    background = ndb.StringProperty(indexed=False, required=True)
    title = ndb.StringProperty(indexed=False, required=True)
    studio = ndb.StringProperty(indexed=False, required=True)


# global variable to track database status
# when the database is created, this flag will be toggled to true
DATABASE_CREATED = False

# global variable to track movie's id
# for same video, the MovieClip and MovieOverview will share the same id
movie_id = 0

# All movies' meta information
MOVIES = """

  [{
    "category": "Google+",
    "videos": [{
      "description": "Jon introduces Instant Upload with a few thoughts on how we remember the things that matter. Check out some ways we've been rethinking real-life sharing for the web at plus.google.com",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Instant%20Upload.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Instant%20Upload/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Instant%20Upload/bg.jpg",
      "title": "Instant Upload",
      "studio": "Google+"
    }, {
      "description": "With Google+ Instant Upload, every picture you take on your phone is instantly backed up to a private Google+ album. It's a simple way to make sure you never lose another memory.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20New%20Dad.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20New%20Dad/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20New%20Dad/bg.jpg",
      "title": "New Dad",
      "studio": "Google+"
    }, {
      "description": "Laugh, share news, celebrate, learn something new or stay in touch with Hangouts. And with Hangouts on your phone, you can drop in from wherever you are.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Say%20more%20with%20Hangouts.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Say%20more%20with%20Hangouts/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Say%20more%20with%20Hangouts/bg.jpg",
      "title": "Say more with Hangouts",
      "studio": "Google+"
    }, {
      "description": "Search on Google+ helps you get advice from the people you know -- sometimes when you least expect it. Check out some ways we've been rethinking real-life sharing for the web at plus.google.com.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Search.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Search/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Search/bg.jpg",
      "title": "Google+ Search",
      "studio": "Google+"
    }, {
      "description": "New ways of sharing the right things with the right people. Join at http://google.com/+",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Sharing%20but%20like%20real%20life.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Sharing%20but%20like%20real%20life/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Sharing%20but%20like%20real%20life/bg.jpg",
      "title": "Sharing but like real life",
      "studio": "Google+"
    }, {
      "description": "Jed introduces Circles with a few thoughts on the nature of friendship. Check out some ways we've been rethinking real-life sharing for the web at plus.google.com.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Circles.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Circles/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Circles/bg.jpg",
      "title": "Google+ Circles",
      "studio": "Google+"
    }, {
      "description": "Aimee introduces Hangouts with a few thoughts on the spontaneous get-together. Check out some ways we've been rethinking real-life sharing for the web at plus.google.com.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Hangouts.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Hangouts/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Google%2B/Google%2B_%20Hangouts/bg.jpg",
      "title": "Google+ Hangouts",
      "studio": "Google+"
    }]
  }, {
    "category": "Demo Slam",
    "videos": [{
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
        "title": "20ft Search",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Balcony%20Toss.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Balcony%20Toss/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Balcony%20Toss/bg.jpg",
        "title": "Balcony Toss",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Dance%20Search.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Dance%20Search/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Dance%20Search/bg.jpg",
        "title": "Dance Search",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Epic%20Docs%20Animation.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Epic%20Docs%20Animation/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Epic%20Docs%20Animation/bg.jpg",
        "title": "Epic Docs Animation",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Extra%20Spicy.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Extra%20Spicy/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Extra%20Spicy/bg.jpg",
        "title": "Extra Spicy",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Get%20Your%20Money's%20Worth.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Get%20Your%20Money's%20Worth/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Get%20Your%20Money's%20Worth/bg.jpg",
        "title": "Get Your Money's Worth",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Guitar%20Search.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Guitar%20Search/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Guitar%20Search/bg.jpg",
        "title": "Guitar Search",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hangin'%20with%20the%20Google%20Search%20Bar.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hangin'%20with%20the%20Google%20Search%20Bar/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hangin'%20with%20the%20Google%20Search%20Bar/bg.jpg",
        "title": "Hangin' with the Google Search Bar",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hometown%20Caroling.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hometown%20Caroling/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hometown%20Caroling/bg.jpg",
        "title": "Hometown Caroling",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Instant%20Music.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Instant%20Music/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Instant%20Music/bg.jpg",
        "title": "Instant Music",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Pep%20Talk.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Pep%20Talk/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Pep%20Talk/bg.jpg",
        "title": "Pep Talk",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Realtime%20Karaoke.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Realtime%20Karaoke/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Realtime%20Karaoke/bg.jpg",
        "title": "Realtime Karaoke",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Revis%20posterizes%20Stoudemire.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Revis%20posterizes%20Stoudemire/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Revis%20posterizes%20Stoudemire/bg.jpg",
        "title": "Revis posterizes Stoudemire",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Route%2066.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Route%2066/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Route%2066/bg.jpg",
        "title": "Route 66",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Rushmore.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Rushmore/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Rushmore/bg.jpg",
        "title": "Rushmore",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Shopping%20Cart.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Shopping%20Cart/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Shopping%20Cart/bg.jpg",
        "title": "Shopping Cart",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Stealing%20the%20Logo.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Stealing%20the%20Logo/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Stealing%20the%20Logo/bg.jpg",
        "title": "Stealing the Logo",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Stoudemire%20slams%20Revis.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Stoudemire%20slams%20Revis/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Stoudemire%20slams%20Revis/bg.jpg",
        "title": "Stoudemire slams Revis",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Streetview%20Road%20Race.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Streetview%20Road%20Race/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Streetview%20Road%20Race/bg.jpg",
        "title": "Streetview Road Race",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Thanksgiving%20Goggles.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Thanksgiving%20Goggles/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Thanksgiving%20Goggles/bg.jpg",
        "title": "Thanksgiving Goggles",
        "studio": "Google Demo Slam"
      }, {
        "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
        "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Video%20Chat%20Magic.mp4"],
        "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Video%20Chat%20Magic/card.jpg",
        "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Video%20Chat%20Magic/bg.jpg",
        "title": "Video Chat Magic",
        "studio": "Google Demo Slam"
      }
    ]
  }, {
    "category": "Gone Google",
    "videos": [{
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Google%20Drive.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Google%20Drive/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Google%20Drive/bg.jpg",
      "title": "Google Drive has Gone Google",
      "studio": "Gone Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Hall%20and%20Oates.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Hall%20and%20Oates/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Go%20Google_%20Hall%20and%20Oates/bg.jpg",
      "title": "Hall and Oates has Gone Google",
      "studio": "Gone Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Pt%20England%20School%20has%20Gone%20Google.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Pt%20England%20School%20has%20Gone%20Google/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Pt%20England%20School%20has%20Gone%20Google/bg.jpg",
      "title": "Pt England School has Gone Google",
      "studio": "Gone Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Work%20has%20gone%20Google.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Work%20has%20gone%20Google/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Gone%20Google/Work%20has%20gone%20Google/bg.jpg",
      "title": "Work has Gone Google",
      "studio": "Gone Google"
    }]
  }, {
    "category": "Zeitgeist",
    "videos": [{
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20-%202013%20in%20Searches.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20-%202013%20in%20Searches/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20-%202013%20in%20Searches/bg.jpg",
      "title": "2013 in Searches",
      "studio": "Google Zeitgeist"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
      "title": "Year In Review - 2010",
      "studio": "Google Zeitgeist"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202011_%20Year%20In%20Review.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202011_%20Year%20In%20Review/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202011_%20Year%20In%20Review/bg.jpg",
      "title": "Year In Review - 2011",
      "studio": "Google Zeitgeist"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202012_%20Year%20In%20Review.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202012_%20Year%20In%20Review/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202012_%20Year%20In%20Review/bg.jpg",
      "title": "Year In Review - 2012",
      "studio": "Google Zeitgeist"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20_%20Here's%20to%202013.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20_%20Here's%20to%202013/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Google%20Zeitgeist%20_%20Here's%20to%202013/bg.jpg",
      "title": "Here's to 2013",
      "studio": "Google Zeitgeist"
    }]
  }, {
    "category": "April Fool's 2013",
    "videos": [{
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Explore%20Treasure%20Mode%20with%20Google%20Maps.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Explore%20Treasure%20Mode%20with%20Google%20Maps/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Explore%20Treasure%20Mode%20with%20Google%20Maps/bg.jpg",
      "title": "Explore Treasure Mode with Google Maps",
      "studio": "Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
      "title": "Introducing Gmail Blue",
      "studio": "Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
      "title": "Introducing Google Fiber to the Pole",
      "studio": "Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
      "title": "Introducing Google Nose",
      "studio": "Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/YouTube's%20ready%20to%20select%20a%20winner.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/YouTube's%20ready%20to%20select%20a%20winner/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/YouTube's%20ready%20to%20select%20a%20winner/bg.jpg",
      "title": "YouTube's ready to select a winner",
      "studio": "Google"
    }, {
      "description": "Fusce id nisi turpis. Praesent viverra bibendum semper. Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.",
      "sources": ["https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Levity%20Algorithm.mp4"],
      "card": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Levity%20Algorithm/card.jpg",
      "background": "https://storage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Levity%20Algorithm/bg.jpg",
      "title": "Levity Algorithm",
      "studio": "Google"
    }]
  }]

"""


# handler to get all videos in same category
# the category information will be passed from the url
class GetVideosInSameCategory(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        category = self.request.get('category')
        # we will randomize the database every time when the request is received by server
        if random.random() > 0.5:

            # in this randomize method, for category x, we will copy all the movie from category x
            # and create a new category "x_dup"
            # also for all the movie/ movie overview in category "x", it will be duplicated
            duplicate_category(category)
        else:

            # in this randomize method, for category x, if the duplicated category is existed
            # i.e. if category "x_dup" is existed, we will remove this category and all video clips
            # in this category (the movie, movie overview and category are put in different table,
            # but they are associated with each other)
            # also for all the movie/ movie overview in category "x", it will be duplicated
            remove_duplicate_category(category)
        movies = get_videos_in_same_category(category)

        # we will shuffle the result every time for testing purpose
        random.shuffle(movies)

        # return the result in json format
        self.response.write(
            json.dumps([mov.to_dict() for mov in movies])
        )


# handler to get all video's categories
class GetAllCategory(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        categories = getAllCategory()

        # we will chose a category randomly and change it's name randomly
        random_index = random.randrange(0, len(categories))
        random.shuffle(categories)
        old_categoryName = categories[random_index].category

        # generate a new category name based on it's previous name
        # it could be one of the following two scenarios
        # 1. the last character is removed
        # 2. the '_new' is append at the end
        if random.random() > 0.5:
            new_categoryName = old_categoryName[0:len(old_categoryName) - 1]
        else:
            new_categoryName = old_categoryName + '_new'

        # when the new category name is valid, update the category and all movies/ movies overview in this category
        if new_categoryName:
            update_category_name(old_category=old_categoryName, new_category=new_categoryName)
            categories = getAllCategory()
            random.shuffle(categories)

        # return the reuslt in json format
        self.response.write(
            json.dumps([cat.to_dict() for cat in categories])
        )


# handler to handle get specific video through id request
class GetVideoById(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        # get id information from url
        id = self.request.get('id')

        # the detailed model will be returned in this situation to render the detail video view activity
        movie = get_detailed_video_by_id(int(id))

        # return the result in json format
        self.response.write(
            json.dumps(movie.to_dict())
        )


# handler to unrent the video
class UnRentVideo(webapp2.RequestHandler):
    # using post method to handle this request
    def post(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        # extract id information from post url
        id = int(self.request.get('id'))
        unrent_video_by_id(id)


# handler to rent the video
class RentVideo(webapp2.RequestHandler):
    def post(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        id = int(self.request.get('id'))
        rent_video_by_id(id)

# handler to create database
# shouldn't be called by client
class CreateDb(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        global DATABASE_CREATED
        if not DATABASE_CREATED:
            createDataBase()
            DATABASE_CREATED = True
            self.response.write("Database Created")


# handler to clear database
# shouldn't be called by client
class ClearDb(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        global DATABASE_CREATED
        if DATABASE_CREATED:
            clear_database()
            self.response.write("Database Cleared")
            DATABASE_CREATED = False


# handler to duplicate specific category
# only used for testing purpose, shouldn't be called by client
class DuplicateCategory(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        test_channel = "Google+"
        duplicate_category(test_channel)
        self.response.write("Duplicated Channel Created")


# handler to remove a specific category
# only used for testing purpose, shouldn't be called by client
class RemoveDuplicatedCategory(webapp2.RequestHandler):
    def get(self):
        # add artificial random latency
        time.sleep(generateRandomLatency())

        test_channel = "Google+"
        remove_duplicate_category(test_channel)
        self.response.write("Duplicated Channel Removed")


# helper function to create database
# shouldn't be called by client directly
def createDataBase():
    videosCapturedInCategory = json.loads(MOVIES)
    global movie_id
    for each in videosCapturedInCategory:
        cat = Category(category=each['category'])
        cat.put()
        for each_video in each['videos']:
            video = MovieClip(id=movie_id,
                              category=cat.category,
                              description=each_video["description"], source=each_video["sources"][0],
                              card=each_video["card"], background=each_video["background"],
                              title=each_video["title"], studio=each_video["studio"], rented=False)
            videoOverview = MovieOverview(id=movie_id,
                                          category=cat.category,
                                          source=each_video["sources"][0],
                                          card=each_video["card"], background=each_video["background"],
                                          title=each_video["title"], studio=each_video["studio"])
            videoOverview.put()
            video.put()
            movie_id += 1


# helper function to find MovieClip through specific id
def get_detailed_video_by_id(id):
    return MovieClip.query(MovieClip.id == id).fetch()[0]



# helper function to clear the whole database
# we have three tables in our database: MovieClip/ MovieClipOverview/ Category
# all those information will be cleared through this function
def clear_database():

    # clear the category table
    for category in Category.query().fetch():
        category.key.delete()

    # clear the MovieClip table
    for video in MovieClip.query().fetch():
        video.key.delete()

    # clear the overview table
    for overview in MovieOverview.query().fetch():
        overview.key.delete()


# helper function duplicate the category
def duplicate_category(category):
    movies_in_same_category_query = MovieClip.query(MovieClip.category == category)
    movies = movies_in_same_category_query.fetch()

    # create a new category named as "previous_category_name" + "_dup"
    new_category = category + "_dup"
    duplicated_category = Category(category=new_category)
    duplicated_category.put()
    global movie_id
    for each in movies:

        # create new movie/ movie overview in the new category
        new_movie_overview = MovieOverview(id=movie_id,
                                           category=new_category,
                                           source=each.source,
                                           card=each.card, background=each.background,
                                           title=each.title, studio=each.studio)
        new_movie = MovieClip(id=movie_id,
                              category=new_category,
                              description=each.description, source=each.source,
                              card=each.card, background=each.background,
                              title=each.title, studio=each.studio, rented=False)
        # the overview and movie should share the same id, so the id will only be incremented once
        movie_id += 1

        # insert the same video clip/ overview in the previous category
        movie_in_same_category_duplicate_overview = MovieOverview(id=movie_id,
                                                                  category=category,
                                                                  source=each.source,
                                                                  card=each.card, background=each.background,
                                                                  title=each.title, studio=each.studio)

        movie_in_same_category_duplicate = MovieClip(id=movie_id,
                                                     category=category,
                                                     description=each.description, source=each.source,
                                                     card=each.card, background=each.background,
                                                     title=each.title, studio=each.studio, rented=False)

        # the overview and movie should share the same id, so the id will only be incremented once
        movie_id += 1

        # insert them in database
        new_movie.put()
        new_movie_overview.put()
        movie_in_same_category_duplicate.put()
        movie_in_same_category_duplicate_overview.put()


# helper function to remove the artificial duplicated category and all the related videos
def remove_duplicate_category(category):
    duplicated_category = category + "_dup"
    if len(Category.query(Category.category == duplicated_category).fetch()) > 0:

        # remove the selected category from category table
        Category.query(Category.category == duplicated_category).fetch()[0].key.delete()
        movies_in_same_category_query = MovieClip.query(MovieClip.category == duplicated_category)
        movie_overviews_in_same_category_qeury = MovieOverview.query(MovieOverview.category == duplicated_category)
        movies = movies_in_same_category_query.fetch()

        # remove all associate movies
        movie_overviews = movie_overviews_in_same_category_qeury.fetch()
        for each in movies:
            each.key.delete()

        # remove all associate overviews
        for each in movie_overviews:
            each.key.delete()


# helper function to unrent the video
# find the MovieClip and update the rented filed in database
def unrent_video_by_id(id):
    found_movie_by_id = MovieClip.query(MovieClip.id == id).fetch()[0]
    if not found_movie_by_id.rented:
        return
    found_movie_by_id.rented = False
    found_movie_by_id.put()




# helper function to rent the video
# find the MovieClip and update the rented filed in database
def rent_video_by_id(id):
    found_movie_by_id = MovieClip.query(MovieClip.id == id).fetch()[0]
    found_movie_by_id.rented = True
    found_movie_by_id.put()
    return found_movie_by_id


# helper function to find all videos in same category
# the return type is ndb data store
def get_videos_in_same_category(category):
    return MovieOverview.query(MovieClip.category == category).fetch()


# helper function to return all videos
# will be used by search functionality
def get_all_videos(category):
    return MovieOverview.query().fetch()


# helper function to get all videos
def getAllCategory():
    return Category.query().fetch()


# helper function to update category's name
# as well as all the movie clip/ movie overview in that category
def update_category_name(old_category, new_category):
    category = Category.query(Category.category == old_category).fetch()[0]

    # update the category information in the database
    category.category = new_category
    category.put()

    # find all movie clips in current category
    movies_in_same_category_query = MovieClip.query(MovieClip.category == old_category)
    movies = movies_in_same_category_query.fetch()

    # find all overviews in current category
    movie_overviews_in_same_category_query = MovieClip.query(MovieClip.category == old_category)
    movie_overviews = movie_overviews_in_same_category_query.fetch()

    # update movie clip's category information
    for movie_clip in movies:
        movie_clip.category = new_category
        movie_clip.put()

    # update overview's category information
    for overview in movie_overviews:
        overview.category = new_category
        overview.put()


# helper function to generate a random number to add artificial network request latency
def generateRandomLatency():
    return random.randrange(2, 5)

# create handler and url mapping
app = webapp2.WSGIApplication([
    # shouldn't be used by client
    ('/create', CreateDb),

    # shouldn't be used by client
    ('/clear', ClearDb),

    # shouldn't be used by client
    ('/duplicate_test_add', DuplicateCategory),

    # shouldn't be used by client
    ('/duplicate_test_remove', RemoveDuplicatedCategory),

    # server's api
    ('/get_all_categories', GetAllCategory),
    ('/get_videos_by_category', GetVideosInSameCategory),
    ('/get_video_by_id', GetVideoById),
    ('/rent_video', RentVideo),
    ('/un_rent_video', UnRentVideo),
], debug=True)
